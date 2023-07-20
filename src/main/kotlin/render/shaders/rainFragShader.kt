package render.shaders

import render.Screen

fun rainFragShader() = """
    #version ${Screen.GLSL_VERSION}
    uniform sampler2D u_RainMask;
    uniform sampler2D u_SnowMask;
    uniform float u_Time;
    uniform float u_RainSpeed;
    uniform float u_SnowSpeed;

    varying vec2 v_UV;
    varying float v_RainAlpha;
    varying float v_SnowAlpha;

    void main()
    {
        vec2 fallUV = vec2(v_UV.x, v_UV.y + u_Time * u_RainSpeed * 0.57);
        vec2 fallUV2 = vec2(v_UV.x + 0.2, v_UV.y + 0.2 + u_Time * u_RainSpeed * 0.72);
        vec2 fallUV3 = vec2(v_UV.x + 0.46, v_UV.y + 0.46 + u_Time * u_RainSpeed * 1.11);
        vec4 sample = texture2D(u_RainMask, fallUV);
        vec4 sample2 = texture2D(u_RainMask, fallUV2);
        vec4 sample3 = texture2D(u_RainMask, fallUV3);
        sample.a = min(1.0, sample.a * v_RainAlpha / 0.4);
        sample2.a = min(1.0, sample2.a * max(0.0, v_RainAlpha - 0.3) / 0.4);
        sample3.a = min(1.0, sample3.a * max(0.0, v_RainAlpha - 0.6) / 0.4);
        vec4 rainFC = vec4(sample.r + sample2.r + sample3.r, sample.g + sample2.g + sample3.g, sample.b + sample2.b + sample3.b, min(1.0, sample.a + sample2.a + sample3.a) * 0.15);
        
        vec2 snowUV = vec2(v_UV.x, v_UV.y + u_Time * u_SnowSpeed * 0.57);
        vec2 snowUV2 = vec2(v_UV.x + 0.2, v_UV.y + 0.2 + u_Time * u_SnowSpeed * 0.72);
        vec2 snowUV3 = vec2(v_UV.x + 0.46, v_UV.y + 0.46 + u_Time * u_SnowSpeed * 1.11);
        vec4 ss = texture2D(u_SnowMask, snowUV);
        vec4 ss2 = texture2D(u_SnowMask, snowUV2);
        vec4 ss3 = texture2D(u_SnowMask, snowUV3);
        ss.a = min(1.0, ss.a * v_SnowAlpha / 0.4);
        ss2.a = min(1.0, ss2.a * max(0.0, v_SnowAlpha - 0.3) / 0.4);
        ss3.a = min(1.0, ss3.a * max(0.0, v_SnowAlpha - 0.6) / 0.4);
        vec4 snowFC = vec4(ss.r + ss2.r + ss3.r, ss.g + ss2.g + ss3.g, ss.b + ss2.b + ss3.b, min(1.0, ss.a + ss2.a + ss3.a) * 0.35);
        
        gl_FragColor = vec4(min(1.0, rainFC.r + snowFC.r), min(1.0, rainFC.g + snowFC.g), min(1.0, rainFC.b + snowFC.b), min(1.0, rainFC.a + snowFC.a));
    }

""".trimIndent()
