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
	float depth = vPosition_cameraspace.z / vPosition_cameraspace.w;
	
	// scale depth from [-1 .. 1] to [0 .. 255]
	float depthScaled = 127.5 * (depth + 1.0);
	// set red to high byte
	float red = floor(depthScaled) / 255.0;
	// set green to low byte
	float green = depthScaled - floor(depthScaled);
	
	gl_FragColor = vec4(red, green, 0.0, 1.0);
}