/*
 * A gouraud fragment shader that supports a single light source and texture mapping.
 *
 * @author fabmax
 */

precision mediump float;

uniform sampler2D uTextureSampler;
uniform float uAlpha;

varying vec2 vTexCoord;

void main() {
    vec4 color = texture2D(uTextureSampler, vTexCoord);
    color.a *= uAlpha;
	gl_FragColor = color;
}