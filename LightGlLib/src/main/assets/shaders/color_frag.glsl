/*
 * The simplest fragment shader possible - just sets a fixed color.
 *
 * @author fabmax
 */

precision mediump float;

varying vec3 vFragmentColor;

void main() {
	gl_FragColor = vec4(vFragmentColor, 1);
}