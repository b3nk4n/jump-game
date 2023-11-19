#ifdef GL_ES
    precision mediump float;
#endif

uniform float u_time;
uniform float u_width;
uniform float u_opacity;
uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    float speed = u_time * 4.0;
    float amplitude = 0.0015;
    float freq = u_width * 6.66;
    float x = v_texCoords.x;
    float y = v_texCoords.y + (sin(x * freq + speed) * amplitude) - (sin((-x * freq + speed) * 0.66) * amplitude * 0.66);
    gl_FragColor = vec4(v_color.rgb, u_opacity) * texture2D(u_texture, vec2(x, y));
}