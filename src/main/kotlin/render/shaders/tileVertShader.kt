package render.shaders

fun tileVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_TexCoordinate;
    attribute vec3 a_Light;

    varying vec2 v_TexCoordinate;
    varying vec3 v_Light;

    void main()
    {
        v_TexCoordinate = a_TexCoordinate;
        v_Light = a_Light;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
