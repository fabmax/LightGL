/*
 * A shader for rendering depth textures. This is a workaround for hardware that does not
 * support rendering to a depth texture directly. 
 *
 * @author fabmax
 */

uniform mat4 uMvpMatrix;

attribute vec3 aVertexPosition_modelspace;

varying vec4 vPosition_cameraspace;

void main() {
	vPosition_cameraspace = uMvpMatrix * vec4(aVertexPosition_modelspace, 1);
    gl_Position = vPosition_cameraspace;
}