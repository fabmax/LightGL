/*
 * A gouraud fragment shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;

varying vec2 vTexCoord;
varying vec4 vDiffuseLightColor;
varying vec4 vSpecularLightColor;

void main() {
	// Get base fragment color from texture
	vec4 fragmentColor = texture2D(uTextureSampler, vTexCoord);

	vec4 materialAmbientColor = fragmentColor * vec4(0.4, 0.4, 0.4, 1.0);
	vec4 materialDiffuseColor = fragmentColor * vDiffuseLightColor;
	vec4 materialSpecularColor = vSpecularLightColor;

	// compute output color
	gl_FragColor = materialAmbientColor + materialDiffuseColor + materialSpecularColor;
}