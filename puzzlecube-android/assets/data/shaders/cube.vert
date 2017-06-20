#ifdef GL_ES
	precision mediump float;
	precision mediump int;
#endif

uniform mat4 u_camera;
uniform mat4 u_rotation;

attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute vec4 a_color;

varying vec2 v_texCoord;
varying vec4 v_color;

void main() {
	v_texCoord = a_texCoord;
	v_color = a_color;

	gl_Position = (u_camera * u_rotation) * a_position;
}