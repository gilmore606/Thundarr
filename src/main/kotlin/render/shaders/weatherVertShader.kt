package render.shaders

fun weatherVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_TexCoordinate;
    attribute float a_Alpha;

    varying vec2 v_TexCoordinate;
    varying float v_Alpha;

    void main()
    {
        v_TexCoordinate = a_TexCoordinate;
        v_Alpha = a_Alpha;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
