/*
 * A shader for rendering depth textures. This is a workaround for hardware that does not
 * support rendering to a depth texture directly. Depth information is stored in red and
 * green color channels.
 *
 * @author fabmax
 */

precision mediump float;

varying vec4 vPosition_cameraspace;

void main() {
	// pack fragment depth into red and green color channels
	float depth = vPosition_cameraspace.z / vPosition_cameraspace.w;
	float depthScaled = 64.0 * depth + 64.0;
	float red = floor(depthScaled) / 128.0;
	float green = depthScaled - floor(depthScaled);
	gl_FragColor = vec4(red, green, 0.0, 1.0);
}