package render.shaders

fun tileFragShader() = """
    uniform sampler2D u_Texture;

    varying vec2 v_TexCoordinate;
    varying vec3 v_Light;

    void main()
    {
        gl_FragColor = texture2D(u_Texture, v_TexCoordinate) * vec4(v_Light.xyz, 1.0);
    }

""".trimIndent()
