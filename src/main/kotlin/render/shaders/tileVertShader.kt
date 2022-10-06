package render.shaders

fun tileVertShader() = """
    attribute vec3 a_Position;
    attribute vec2 a_TexCoordinate;
    attribute float a_Visibility;

    varying vec2 v_TexCoordinate;
    varying float v_Visibility;

    void main()
    {
        // Transform the vertex into eye space.
        //v_Position = vec3(u_MVMatrix * a_Position);

        v_TexCoordinate = a_TexCoordinate;
        v_Visibility = a_Visibility;

        gl_Position = vec4(a_Position.xy, 0.0, 1.0);
    }

""".trimIndent()
