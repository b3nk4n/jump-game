#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_granularity;
uniform vec2 u_imageSize;
uniform float u_brightness;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec2 relativePos = v_texCoords;

    if (u_granularity > 1.0)
    {
        float dx = u_granularity / u_imageSize.x;
        float dy = u_granularity / u_imageSize.y;
        relativePos = vec2(dx * (floor(relativePos.x / dx) + 0.5),
                           dy * (floor(relativePos.y / dy) + 0.5));
    }

    vec4 color = v_color * texture2D(u_texture, relativePos).rgba;
    gl_FragColor = mix(vec4(0.0, 0.0, 0.0, 1.0), color, clamp(u_brightness, 0.0, 1.0));
}
