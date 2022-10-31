package render.shaders

fun cloudFragShader() = """
    uniform sampler2D u_CloudTexture;

    varying vec2 v_CloudUV;
    varying float v_CloudAlpha;

    void main()
    {
        vec4 sample = texture2D(u_CloudTexture, v_CloudUV);
        float cirrus = max(0.0, v_CloudAlpha - 0.1);
        vec4 sample2 = texture2D(u_CloudTexture, vec2(v_CloudUV.x * 0.35, v_CloudUV.y * 0.42)) * cirrus;
        float value = sample.r - sample2.r;
        gl_FragColor = vec4(value, value, value, (sample.a + sample2.a) * v_CloudAlpha);
    }

""".trimIndent()
