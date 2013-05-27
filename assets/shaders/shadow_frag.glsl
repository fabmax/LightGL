/*
 * A phong fragment shader that supports a single light source and fixed vertex colors.
 * Inspired by http://www.opengl-tutorial.org/beginners-tutorials/tutorial-8-basic-shading/
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

float shadow2D(vec4 coord) {
	float visibility = 0.0;
	float depth = (vShadowCoord.z - 0.025) / vShadowCoord.w;
	
	vec4 shadowValue = texture2D(uShadowSampler, coord.xy);
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility += 1.0;
	}
	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.002, coord.y + 0.002));
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility += 1.0;
	}
	shadowValue = texture2D(uShadowSampler, vec2(coord.x + 0.002, coord.y - 0.002));
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility += 1.0;
	}
	shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.002, coord.y - 0.002));
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility += 1.0;
	}
	shadowValue = texture2D(uShadowSampler, vec2(coord.x - 0.002, coord.y + 0.002));
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility += 1.0;
	}
	
	return clamp(visibility / 5.0, 0.2, 1.0);
}

float shadow2DSimple(vec4 coord) {
	float visibility = 0.2;
	float depth = (vShadowCoord.z - 0.025) / vShadowCoord.w;
	
	vec4 shadowValue = texture2D(uShadowSampler, coord.xy);
	if ((shadowValue.r + shadowValue.g / 255.0) > depth) {
		visibility = 1.0;
	}
	
	return visibility;
}

void main() {
	// normalize input vectors
	vec3 e = normalize(vEyeDirection_cameraspace);
	vec3 l = normalize(vLightDirection_cameraspace);
	vec3 n = normalize(vNormal_cameraspace);

	// high quality shadows but expensive
	float visibility = shadow2D(vShadowCoord);
	// low quality shadows but faster
	//float visibility = shadow2DSimple(vShadowCoord);

	// Cosine of angle between surface normal and light direction
	float cosTheta = clamp(dot(n, l), 0.0, 1.0);
	
	// Ambient color is the fragment color in dark
	// useful for debugging: vec3 fragmentColor = texture2D(uShadowSampler, vShadowCoord.xy).rgb;
	vec3 fragmentColor = texture2D(uTextureSampler, vTexCoord).rgb;
	vec3 materialAmbientColor = vec3(0.2, 0.2, 0.2) * fragmentColor;
	
	vec3 materialDiffuseColor = fragmentColor * uLightColor * cosTheta;
	
	// Direction in which the light is reflected
	vec3 r = reflect(-l, n);
	// Cosine of the angle between the eye vector and the reflect vector
	float cosAlpha = clamp(dot(e, r), 0.0, 1.0);
	vec3 materialSpecularColor = uLightColor * pow(cosAlpha, uShininess);

	// compute output color
	gl_FragColor.rgb = materialAmbientColor + (materialDiffuseColor + materialSpecularColor ) * visibility;
}
