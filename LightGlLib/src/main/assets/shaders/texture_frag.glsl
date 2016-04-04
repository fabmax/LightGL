/*
 * A gouraud fragment shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;

varying vec2 vTexCoord;

void main() {
	gl_FragColor = texture2D(uTextureSampler, vTexCoord);
}