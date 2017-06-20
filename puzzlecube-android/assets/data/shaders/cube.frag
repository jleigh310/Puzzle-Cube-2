#ifdef GL_ES
	precision mediump float;
	precision mediump int;
#endif

uniform sampler2D u_texture;
uniform vec4 u_color;

varying vec2 v_texCoord;
varying vec4 v_color;

void main() {
	gl_FragColor = (u_color * v_color) * texture2D(u_texture, v_texCoord);
}