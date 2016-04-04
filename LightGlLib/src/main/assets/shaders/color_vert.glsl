/*
 * A very simple vertex shader. No textures, no lights, no bullshit.
 *
 * @author fabmax
 */

uniform mat4 uMvpMatrix;

attribute vec3 aVertexPosition_modelspace;
attribute vec4 aVertexColor;

varying vec4 vFragmentColor;

void main() {
	vFragmentColor = aVertexColor;
    gl_Position = uMvpMatrix * vec4(aVertexPosition_modelspace, 1);
}