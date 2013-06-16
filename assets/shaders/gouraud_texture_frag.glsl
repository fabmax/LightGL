/*
 * A gouraud fragment shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;

varying vec2 vTexCoord;
varying vec3 vDiffuseLightColor;
varying vec3 vSpecularLightColor;

void main() {
	// Get base fragment color from texture
	vec3 fragmentColor = texture2D(uTextureSampler, vTexCoord).rgb;
	
	vec3 materialAmbientColor = fragmentColor * vec3(0.2, 0.2, 0.2);
	vec3 materialDiffuseColor = fragmentColor * vDiffuseLightColor;
	vec3 materialSpecularColor = vSpecularLightColor;

	// compute output color
	gl_FragColor.rgb = materialAmbientColor +
					   materialDiffuseColor +
					   materialSpecularColor;
}