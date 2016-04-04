/*
 * A gouraud vertex shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

uniform mat4 uMvpMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;

attribute vec3 aVertexPosition_modelspace;
attribute vec2 aVertexTexCoord;

varying vec2 vTexCoord;

void main() {
	// interpolate vertex color for usage in fragment shader
	vTexCoord = aVertexTexCoord;
	
	// Output position of the vertex in clip space : MVP * position
    gl_Position = uMvpMatrix * vec4(aVertexPosition_modelspace, 1);
}