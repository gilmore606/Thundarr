package render.shaders

fun tileFragShader() = """
    uniform sampler2D u_Texture;

    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;
    varying float v_Hue;

    void main()
    {
        vec4 sample = texture2D(u_Texture, v_TexCoordinate) * v_Light;
        vec3 color = vec3(sample.r, sample.g, sample.b);
        
        const vec3 k = vec3(0.57735, 0.57735, 0.57735);
        float cosAngle = cos(v_Hue);
        vec3 hueShifted = vec3(color * cosAngle + cross(k, color) * sin(v_Hue) + k * dot(k, color) * (1.0 - cosAngle));
        
        float grey = 0.21 * hueShifted.r + 0.71 * hueShifted.g + 0.07 * hueShifted.b;
        float white = max(0.0, v_Grayout - 1.0);
        gl_FragColor = vec4(hueShifted.r * (1.0 - v_Grayout) + grey * v_Grayout + white, hueShifted.g * (1.0 - v_Grayout) + grey * v_Grayout + white, hueShifted.b * (1.0 - v_Grayout) + grey * v_Grayout + white, sample.a);
       
       // scanlines
        if (mod(floor(gl_FragCoord.y), 4.0) == 0.0) {
            gl_FragColor.rgb *= 0.9;
        }
    }
    
    vec3 hueShift(vec3 color, float hue)
    {
        const vec3 k = vec3(0.57735, 0.57735, 0.57735);
        float cosAngle = cos(hue);
        return vec3(color * cosAngle + cross(k, color) * sin(hue) + k * dot(k, color) * (1.0 - cosAngle));
    }

""".trimIndent()
