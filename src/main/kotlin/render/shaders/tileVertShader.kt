package render.shaders

import render.Screen

fun tileVertShader() = """
    #version ${Screen.GLSL_VERSION}
    uniform float u_Time;
    uniform float u_Zoom;
    uniform float u_CameraX;
    uniform float u_CameraY;
    
    attribute vec3 a_Position;
    attribute vec2 a_TexCoordinate;
    attribute vec4 a_Light;
    attribute float a_Grayout;
    attribute float a_Hue;
    attribute float a_Waves;

    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;
    varying float v_Hue;
    varying float v_Waves;

    void main()
    {
        v_TexCoordinate = a_TexCoordinate;
        v_Light = a_Light;
        v_Grayout = a_Grayout;
        v_Hue = a_Hue;
        v_Waves = a_Waves;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
