/*
 * A gouraud vertex shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

uniform mat4 uMvpMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform vec3 uLightDirection_worldspace;
uniform vec3 uLightColor;
uniform float uShininess;

attribute vec3 aVertexPosition_modelspace;
attribute vec3 aVertexNormal_modelspace;
attribute vec2 aVertexTexCoord;

varying vec2 vTexCoord;
varying vec4 vDiffuseLightColor;
varying vec4 vSpecularLightColor;

void main() {
	// interpolate vertex color for usage in fragment shader
	vTexCoord = aVertexTexCoord;
	
	// Output position of the vertex in clip space : MVP * position
    gl_Position = uMvpMatrix * vec4(aVertexPosition_modelspace, 1);
    
	// Vector from vertex to camera, in camera space. In camera space, the camera is at the origin (0, 0, 0).
	vec3 e = normalize(-(uViewMatrix * uModelMatrix * vec4(aVertexPosition_modelspace, 1)).xyz);
	// Light direction, in camera space. M is left out because light position is already in world space.
	vec3 l = normalize((uViewMatrix * vec4(uLightDirection_worldspace, 0)).xyz);
	// Normal of the the vertex, in camera space
	vec3 n = normalize((uViewMatrix * uModelMatrix * vec4(aVertexNormal_modelspace, 0)).xyz);
	
	// Cosine of angle between surface normal and light direction
	float cosTheta = clamp(dot(n, l), 0.0, 1.0);

	// Direction in which the light is reflected
	vec3 r = reflect(-l, n);
	// Cosine of the angle between the eye vector and the reflect vector
	float cosAlpha = clamp(dot(e, r), 0.0, 1.0);
	
	vDiffuseLightColor = vec4(uLightColor, 1.0)  * (cosTheta + 0.2);
	vSpecularLightColor = vec4(uLightColor, 0.0) * pow(cosAlpha, uShininess);
}