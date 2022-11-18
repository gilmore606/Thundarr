package render.shaders

import render.Screen

fun cloudVertShader() = """
    #version ${Screen.GLSL_VERSION}
    attribute vec3 a_Position;
    attribute vec2 a_CloudUV;
    attribute float a_CloudAlpha;

    varying vec2 v_CloudUV;
    varying float v_CloudAlpha;

    void main()
    {
        v_CloudUV = a_CloudUV;
        v_CloudAlpha = a_CloudAlpha;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
