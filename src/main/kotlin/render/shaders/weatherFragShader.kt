package render.shaders

fun weatherFragShader() = """
    uniform sampler2D u_Texture;

    varying vec2 v_TexCoordinate;
    varying float v_Alpha;

    void main()
    {
        vec4 sample = texture2D(u_Texture, v_TexCoordinate);
        gl_FragColor = vec4(sample.r, sample.g, sample.b, sample.a * v_Alpha);
    }

""".trimIndent()
