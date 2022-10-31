package render.shaders

fun cloudFragShader() = """
    uniform sampler2D u_CloudTexture;

    varying vec2 v_CloudUV;
    varying float v_CloudAlpha;

    void main()
    {
        vec4 sample = texture2D(u_CloudTexture, v_CloudUV);
        float cirrus = max(0.0, v_CloudAlpha - 0.4) * 2;
        vec4 sample2 = texture2D(u_CloudTexture, vec2(v_CloudUV.x + 0.4, v_CloudUV.y + 0.3)) * cirrus;
        float cumulus = max(0.0, v_CloudAlpha - 0.7) * 3.5;
        vec4 sample3 = texture2D(u_CloudTexture, vec2(v_CloudUV.x + 0.63, v_CloudUV.y + 0.41)) * cumulus;
        float value = sample.r - sample2.r - sample3.r;
        gl_FragColor = vec4(value, value, value, min(0.3, (sample.a + sample2.a + sample3.a) * (v_CloudAlpha - 0.05) * 0.5));
    }

""".trimIndent()
