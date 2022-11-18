package render.shaders

import render.Screen

fun rainFragShader() = """
    #version ${Screen.GLSL_VERSION}
    uniform sampler2D u_RainMask;
    uniform float u_Time;
    uniform float u_Speed;

    varying vec2 v_RainUV;
    varying float v_RainAlpha;

    void main()
    {
        vec2 fallUV = vec2(v_RainUV.x, v_RainUV.y + u_Time * u_Speed * 0.57);
        vec2 fallUV2 = vec2(v_RainUV.x + 0.2, v_RainUV.y + 0.2 + u_Time * u_Speed * 0.72);
        vec2 fallUV3 = vec2(v_RainUV.x + 0.46, v_RainUV.y + 0.46 + u_Time * u_Speed * 1.11);
        vec4 sample = texture2D(u_RainMask, fallUV);
        vec4 sample2 = texture2D(u_RainMask, fallUV2);
        vec4 sample3 = texture2D(u_RainMask, fallUV3);
        sample.a = min(1.0, sample.a * v_RainAlpha / 0.4);
        sample2.a = min(1.0, sample2.a * max(0.0, v_RainAlpha - 0.3) / 0.4);
        sample3.a = min(1.0, sample3.a * max(0.0, v_RainAlpha - 0.6) / 0.4);
        gl_FragColor = vec4(sample.r + sample2.r + sample3.r, sample.g + sample2.g + sample3.g, sample.b + sample2.b + sample3.b, min(1.0, sample.a + sample2.a + sample3.a) * 0.15);
    }

""".trimIndent()
