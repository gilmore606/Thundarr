package render.shaders

import render.Screen

fun tileFragShader() = """
    #version ${Screen.GLSL_VERSION}
    uniform sampler2D u_Texture;

    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;
    varying float v_Hue;

    void main()
    {
        vec4 simple = texture2D(u_Texture, v_TexCoordinate);
        vec4 sample = vec4(simple.r, simple.g, simple.b, simple.a * v_Light.a);
        vec3 color = vec3(sample.r, sample.g, sample.b);
        
        const vec3 k = vec3(0.57735, 0.57735, 0.57735);
        float cosAngle = cos(v_Hue);
        vec3 lightColor = vec3(v_Light.r, v_Light.g, v_Light.b);
        vec3 hueShifted = vec3(color * cosAngle + cross(k, color) * sin(v_Hue) + k * dot(k, color) * (1.0 - cosAngle)) * lightColor;
        
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
