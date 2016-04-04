/*
 * A phong fragment shader that supports a single light source with dynamic shadows.
 * Inspired by http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-16-shadow-mapping/
 * However this shader uses a standard texture instead of a depth texture for shadow computation,
 * because depth textures aren't supported on many devices (e.g. my Galaxy Nexus)
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;
uniform float uShininess;
uniform vec3 uLightColor;
uniform sampler2D uShadowSampler;
uniform float uMapScale;

varying vec2 vTexCoord;
varying vec3 vEyeDirection_cameraspace;
varying vec3 vLightDirection_cameraspace;
varying vec3 vNormal_cameraspace;
varying vec4 vShadowCoord;

float shadow2Dsmooth(vec4 coord) {
	float visibility = 4.0;
	float depth = clamp((vShadowCoord.z - 0.01) / vShadowCoord.w, 0.0, 1.0);

	vec4 shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.9420 * uMapScale, coord.y - 0.3990 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);

	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.9456 * uMapScale, coord.y - 0.7689 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.0942 * uMapScale, coord.y - 0.9294 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.3450 * uMapScale, coord.y + 0.2939 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	return clamp(visibility / 5.0, 0.2, 1.0);
}

float shadow2D(vec4 coord) {
	float visibility = 1.0;
	float depth = clamp((vShadowCoord.z - 0.005) / vShadowCoord.w, 0.0, 1.0);
	
	vec4 shadowValue = texture2D(uShadowSampler, coord.xy);
	float d = shadowValue.r + shadowValue.g / 255.0;
	return 1.0 - clamp((depth - d) * 1000.0, 0.0, 0.8);
}

void main() {
	// normalize input vectors
	vec3 e = normalize(vEyeDirection_cameraspace);
	vec3 l = normalize(vLightDirection_cameraspace);
	vec3 n = normalize(vNormal_cameraspace);

	// no shadows
	//float visibility = 1.0;
	// high quality shadows but expensive
	float visibility = shadow2Dsmooth(vShadowCoord);
	// low quality shadows but faster
	//float visibility = shadow2D(vShadowCoord);

	// for diffuse lighting: cosine of angle between surface normal and light direction
	float cosTheta = clamp(dot(n, l), 0.0, 1.0);
	
	// for specular lighting: direction in which the light is reflected
	vec3 r = reflect(-l, n);
	// Cosine of the angle between the eye vector and the reflect vector
	float cosAlpha = clamp(dot(e, r), 0.0, 1.0);

	vec4 fragmentColor = texture2D(uTextureSampler, vTexCoord);

	// Ambient color is the fragment color in dark
	vec4 materialAmbientColor = fragmentColor * vec4(0.4, 0.4, 0.4, 1.0);
	vec4 materialDiffuseColor = fragmentColor * vec4(uLightColor, 1.0) * (cosTheta + 0.2);
	vec4 materialSpecularColor = vec4(uLightColor, 0.0) * pow(cosAlpha, uShininess);

	// compute output color
	gl_FragColor = materialAmbientColor + (materialDiffuseColor + materialSpecularColor) * visibility;
}
