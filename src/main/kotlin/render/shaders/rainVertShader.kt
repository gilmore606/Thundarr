package render.shaders

import render.Screen

fun rainVertShader() = """
    #version ${Screen.GLSL_VERSION}
    attribute vec3 a_Position;
    attribute vec2 a_UV;
    attribute float a_RainAlpha;
    attribute float a_SnowAlpha;
    uniform float u_Time;
    uniform float u_RainSpeed;
    uniform float u_SnowSpeed;

    varying vec2 v_UV;
    varying float v_RainAlpha;
    varying float v_SnowAlpha;

    void main()
    {
        float u = a_UV.x;
        float v = a_UV.y;
        
        v_UV = vec2(u, v);
        v_RainAlpha = a_RainAlpha;
        v_SnowAlpha = a_SnowAlpha;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
