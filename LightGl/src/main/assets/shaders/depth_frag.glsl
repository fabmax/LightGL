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
	
	// for 5-6-5 format
	// scale depth from [-1 .. 1] to [0 .. 31]
	//float depthScaled = 15.5 * (depth + 1.0);
	//float red = floor(depthScaled);
	//depthScaled = 31.5 * (depthScaled - red);
	//float green = floor(depthScaled);
	//float blue = depthScaled - green;
	//red /= 31.0;
	//green /= 63.0;
	//blue /= 31.0;
	
	// scale depth from [-1 .. 1] to [0 .. 255]
	float depthScaled = 127.5 * (depth + 1.0);
	// set red to high byte
	float red = floor(depthScaled);
	// set green to low byte
	float green = depthScaled - red;
	red /= 255.0;
	
	gl_FragColor = vec4(red, green, 0.0, 1.0);
}