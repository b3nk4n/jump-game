#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 u_imageSize;
uniform vec2 u_amplitude;
uniform vec2 u_waveLength;
uniform vec2 u_velocity;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec2 pixelCoords = v_texCoords * u_imageSize;
    vec2 offset = u_amplitude * sin(6.283/u_waveLength * (pixelCoords.yx - u_velocity *  u_time));
    vec2 texCoords = v_texCoords + offset / u_imageSize;
    gl_FragColor = v_color * texture2D(u_texture, texCoords);
}

