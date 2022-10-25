package render.shaders

fun tileVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_TexCoordinate;
    attribute vec4 a_Light;
    attribute float a_Grayout;

    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;

    void main()
    {
        v_TexCoordinate = a_TexCoordinate;
        v_Light = a_Light;
        v_Grayout = a_Grayout;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
