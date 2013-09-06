/*
 * This shader is used to draw a single texture to the screen.
 *
 * @author fabmax
 */

attribute vec3 aVertexPosition_modelspace;
attribute vec2 aVertexTexCoord;

varying vec2 vTexCoord;

void main() {
	vTexCoord = aVertexTexCoord;
    gl_Position = vec4(aVertexPosition_modelspace, 1);
}