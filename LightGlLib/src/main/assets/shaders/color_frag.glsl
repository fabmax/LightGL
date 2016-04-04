/*
 * The simplest fragment shader possible - just sets a fixed color.
 *
 * @author fabmax
 */

precision mediump float;

varying vec4 vFragmentColor;

void main() {
	gl_FragColor = vFragmentColor;
}