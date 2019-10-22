#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_effectRatio;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    vec3 inverted = vec3(1.0 - color.r, 1.0 - color.g, 1.0 - color.b);
    color.rgb = mix(color.rgb, inverted, u_effectRatio);
    gl_FragColor = color;
}
