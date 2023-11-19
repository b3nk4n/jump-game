#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_effectRatio;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    vec3 ratios = vec3(0.2126, 0.7152, 0.0722);
    float grayscale = dot(color.rgb, ratios);
    color.rgb = mix(color.rgb, vec3(grayscale), u_effectRatio);
    gl_FragColor = color;
}
