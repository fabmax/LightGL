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

varying vec2 vTexCoord;
varying vec3 vEyeDirection_cameraspace;
varying vec3 vLightDirection_cameraspace;
varying vec3 vNormal_cameraspace;
varying vec4 vShadowCoord;

float shadow2Dsmooth(vec4 coord) {
	float visibility = 4.0;
	float depth = clamp((vShadowCoord.z - 0.01) / vShadowCoord.w, 0.0, 1.0);
	
	vec4 shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.003768, coord.y - 0.001596));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.003782, coord.y - 0.003076));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.000377, coord.y - 0.003718));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 1000.0, 0.0, 1.0);
	
	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.001380, coord.y + 0.001176));
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
	
	// useful for debugging: vec3 fragmentColor = texture2D(uShadowSampler, vShadowCoord.xy).rgb;
	//vec3 fragmentColor = texture2D(uShadowSampler, vShadowCoord.xy).rgb;
	//fragmentColor.b = clamp((vShadowCoord.z - 0.005) / vShadowCoord.w, 0.0, 1.0);
	vec3 fragmentColor = texture2D(uTextureSampler, vTexCoord).rgb;
	
	vec3 materialAmbientColor = vec3(0.2, 0.2, 0.2) * fragmentColor;
	vec3 materialDiffuseColor = fragmentColor * uLightColor * cosTheta;
	vec3 materialSpecularColor = uLightColor * pow(cosAlpha, uShininess);

	// compute output color
	gl_FragColor.rgb = materialAmbientColor + (materialDiffuseColor + materialSpecularColor ) * visibility;
}
