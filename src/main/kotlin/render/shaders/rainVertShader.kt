package render.shaders

fun rainVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_RainUV;
    attribute float a_RainAlpha;
    uniform float u_Time;
    uniform float u_Speed;

    varying vec2 v_RainUV;
    varying float v_RainAlpha;

    void main()
    {
        float rainu = a_Position.x * 3.0;
        float rainv = a_Position.y * 3.0;
        
        v_RainUV = vec2(rainu, rainv);
        v_RainAlpha = a_RainAlpha;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
