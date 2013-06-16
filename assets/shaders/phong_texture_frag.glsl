/*
 * A phong fragment shader that supports a single light source and texture mapping.
 * Inspired by http://www.opengl-tutorial.org/beginners-tutorials/tutorial-8-basic-shading/
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;
uniform float uShininess;
uniform vec3 uLightColor;

varying vec2 vTexCoord;
varying vec3 vEyeDirection_cameraspace;
varying vec3 vLightDirection_cameraspace;
varying vec3 vNormal_cameraspace;

void main() {
	// normalize input vectors
	vec3 e = normalize(vEyeDirection_cameraspace);
	vec3 l = normalize(vLightDirection_cameraspace);
	vec3 n = normalize(vNormal_cameraspace);

	// Cosine of angle between surface normal and light direction
	float cosTheta = clamp(dot(n, l), 0.0, 1.0);

	// Direction in which the light is reflected
	vec3 r = reflect(-l, n);
	// Cosine of the angle between the eye vector and the reflect vector
	float cosAlpha = clamp(dot(e, r), 0.0, 1.0);
	
	// Get base fragment color from texture
	vec3 fragmentColor = texture2D(uTextureSampler, vTexCoord).rgb;
	
	// Ambient color is the fragment color in dark
	vec3 materialAmbientColor = fragmentColor * vec3(0.2, 0.2, 0.2);
	vec3 materialDiffuseColor = fragmentColor * uLightColor * cosTheta;
	vec3 materialSpecularColor = uLightColor * pow(cosAlpha, uShininess);

	// compute output color
	gl_FragColor.rgb = materialAmbientColor +
					   materialDiffuseColor +
					   materialSpecularColor;
}