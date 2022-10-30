package render.shaders

fun tileFragShader() = """
    uniform sampler2D u_Texture;

    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;

    void main()
    {
        vec4 sample = texture2D(u_Texture, v_TexCoordinate) * v_Light;
        float grey = 0.21 * sample.r + 0.71 * sample.g + 0.07 * sample.b;
        gl_FragColor = vec4(sample.r * (1.0 - v_Grayout) + grey * v_Grayout, sample.g * (1.0 - v_Grayout) + grey * v_Grayout, sample.b * (1.0 - v_Grayout) + grey * v_Grayout, sample.a);
        if (mod(floor(gl_FragCoord.y), 4.0) == 0.0) {
            gl_FragColor.rgb *= 0.8;
        }
    }

""".trimIndent()
