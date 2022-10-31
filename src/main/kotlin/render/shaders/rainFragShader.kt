package render.shaders

fun rainFragShader() = """
    uniform sampler2D u_RainMask;
    uniform float u_Time;
    uniform float u_Speed;

    varying vec2 v_RainUV;
    varying float v_RainAlpha;

    void main()
    {
        vec2 fallUV = vec2(v_RainUV.x, v_RainUV.y + u_Time * u_Speed);
        vec2 fallUV2 = vec2(v_RainUV.x + 0.2, v_RainUV.y + 0.2 + u_Time * u_Speed * 0.41);
        vec2 fallUV3 = vec2(v_RainUV.x + 0.46, v_RainUV.y + 0.46 + u_Time * u_Speed * 0.72);
        float sample = texture2D(u_RainMask, fallUV).r;
        float sample2 = texture2D(u_RainMask, fallUV2).r;
        float sample3 = texture2D(u_RainMask, fallUV3).r;
        gl_FragColor = vec4(0.9, 0.9, 1.0, (sample + sample2 + sample3) * v_RainAlpha * 0.12);
    }

""".trimIndent()
