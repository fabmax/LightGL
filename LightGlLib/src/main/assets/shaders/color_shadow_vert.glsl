/*
 * A very simple vertex shader. No textures, no lights, no bullshit.
 *
 * @author fabmax
 */

uniform mat4 uMvpMatrix;
uniform mat4 uShadowMvpMatrix;

attribute vec3 aVertexPosition_modelspace;
attribute vec4 aVertexColor;

varying vec4 vFragmentColor;
varying vec4 vShadowCoord;

void main() {
	// interpolate vertex color for usage in fragment shader
	vFragmentColor = aVertexColor;

	// compute vertex position in shadow map
	vShadowCoord = uShadowMvpMatrix * vec4(aVertexPosition_modelspace, 1);

	// Output position of the vertex in clip space : MVP * position
    gl_Position = uMvpMatrix * vec4(aVertexPosition_modelspace, 1);
}