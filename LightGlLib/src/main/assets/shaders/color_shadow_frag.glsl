/*
 * The simplest fragment shader possible - just sets a fixed color.
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uShadowSampler;
uniform float uMapScale;

varying vec4 vFragmentColor;
varying vec4 vShadowCoord;

float shadow2Dsmooth(vec4 coord) {
	float visibility = 4.0;
	float depth = clamp((vShadowCoord.z - 0.003) / vShadowCoord.w, 0.0, 1.0);

	vec4 shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.9420 * uMapScale, coord.y - 0.3990 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 300.0, 0.0, 1.0);

	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.9456 * uMapScale, coord.y - 0.7689 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 300.0, 0.0, 1.0);

	shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.0942 * uMapScale, coord.y - 0.9294 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 300.0, 0.0, 1.0);

	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.3450 * uMapScale, coord.y + 0.2939 * uMapScale));
	visibility -= clamp((depth - (shadowValue.r + shadowValue.g / 255.0)) * 300.0, 0.0, 1.0);

	return clamp(visibility / 5.0 + 0.2, 0.25, 0.55) + 0.45;
}

void main() {
	float visibility = shadow2Dsmooth(vShadowCoord);
	//float visibility = 1.0;
	gl_FragColor = vec4(vFragmentColor.rgb * visibility, vFragmentColor.a);
}