package render.shaders

fun cloudFragShader() = """
    uniform sampler2D u_CloudTexture;

    varying vec2 v_CloudUV;
    varying float v_CloudAlpha;

    void main()
    {
        vec4 sample = texture2D(u_CloudTexture, v_CloudUV);
        gl_FragColor = vec4(sample.r, sample.g, sample.b, sample.a * v_CloudAlpha);
    }

""".trimIndent()
