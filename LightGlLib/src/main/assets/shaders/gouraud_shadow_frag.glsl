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
uniform sampler2D uShadowSampler;
uniform float uMapScale;

varying vec2 vTexCoord;
varying vec4 vShadowCoord;
varying vec3 vDiffuseLightColor;
varying vec3 vSpecularLightColor;

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
	// no shadows
	//float visibility = 1.0;
	// high quality shadows but expensive
	float visibility = shadow2Dsmooth(vShadowCoord);
	// low quality shadows but faster
	//float visibility = shadow2D(vShadowCoord);
	
	// useful for debugging: vec3 fragmentColor = texture2D(uShadowSampler, vShadowCoord.xy).rgb;
	//vec3 fragmentColor = texture2D(uShadowSampler, vShadowCoord.xy).rgb;
	//fragmentColor.b = clamp((vShadowCoord.z - 0.005) / vShadowCoord.w, 0.0, 1.0);
	vec3 fragmentColor = texture2D(uTextureSampler, vTexCoord).rgb;
	
	vec3 materialAmbientColor = fragmentColor * vec3(0.2, 0.2, 0.2);
	vec3 materialDiffuseColor = fragmentColor * vDiffuseLightColor;
	vec3 materialSpecularColor = vSpecularLightColor;

	// compute output color
	gl_FragColor.rgb = materialAmbientColor + (materialDiffuseColor + materialSpecularColor ) * visibility;
}
