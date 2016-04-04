/*
 * A phong fragment shader that supports a single light source and fixed vertex colors.
 * Inspired by http://www.opengl-tutorial.org/beginners-tutorials/tutorial-8-basic-shading/
 *
 * @author fabmax
 */

precision mediump float;

uniform float uShininess;
uniform vec3 uLightColor;

varying vec4 vFragmentColor;
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

	vec4 materialAmbientColor = vFragmentColor * vec4(0.4, 0.4, 0.4, 1.0);
	vec4 materialDiffuseColor = vFragmentColor * vec4(uLightColor, 1.0) * (cosTheta + 0.2);
	vec4 materialSpecularColor = vec4(uLightColor, 0.0) * pow(cosAlpha, uShininess);

	// compute output color
	gl_FragColor = materialAmbientColor + materialDiffuseColor + materialSpecularColor;
}