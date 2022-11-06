package render.shaders

fun fireVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_FireUV;
    attribute float a_Offset;
    uniform float u_Time;

    varying vec2 UV;
    varying float v_Offset;

    void main()
    {
        UV = a_FireUV;
        v_Offset = a_Offset;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
