package render.shaders

import render.Screen

fun tileFragShader() = """
    #version ${Screen.GLSL_VERSION}
    uniform sampler2D u_Texture;
    uniform float u_Time;
    uniform float u_Zoom;
    uniform float u_CameraX;
    uniform float u_CameraY;
    
    varying vec2 v_TexCoordinate;
    varying vec4 v_Light;
    varying float v_Grayout;
    varying float v_Hue;
    varying float v_Waves;

    vec2 hash2(vec2 p ) 
    {
       return fract(sin(vec2(dot(p, vec2(123.4, 748.6)), dot(p, vec2(547.3, 659.3))))*5232.85324);   
    }
    
    float hash(vec2 p) 
    {
      return fract(sin(dot(p, vec2(43.232, 75.876)))*4526.3257);   
    }
            
    float voronoi(vec2 p) 
    {
        vec2 n = floor(p);
        vec2 f = fract(p);
        float md = 5.0;
        vec2 m = vec2(0.0);
        for (int i = -1;i<=1;i++) {
            for (int j = -1;j<=1;j++) {
                vec2 g = vec2(i, j);
                vec2 o = hash2(n+g);
                o = 0.5+0.5*sin(u_Time+5.038*o);
                vec2 r = g + o - f;
                float d = dot(r, r);
                if (d<md) {
                  md = d;
                  m = n+g+o;
                }
            }
        }
        return md;
    }
    
    float waterOV(vec2 p) 
    {
        float v = 0.0;
        float a = 0.4;
        for (int i = 0;i<3;i++) {
            v+= voronoi(p)*a;
            p*=2.0;
            a*=0.5;
        }
        return v;
    }
    
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
       
       // water waves
       if (v_Waves > 0.0 && v_Grayout == 0.0) {
           vec2 waveUV = gl_FragCoord.xy / vec2(800,600) * 2.0 / u_Zoom;
           waveUV.x += u_CameraX + u_Time * 0.05;
           waveUV.y -= u_CameraY + u_Time * 0.05;
           vec4 waveColor = vec4(0.8,0.9,1.0,1.0);
           vec4 transparent = vec4(0.0,0.0,0.0,0.0);
           float lightLevel = 0.21 * lightColor.r + 0.71 * lightColor.g + 0.07 * lightColor.b;
           vec4 waveOut = vec4(mix(transparent, waveColor, smoothstep(0.0, 0.5, waterOV(waveUV*5.0)))) * 0.15 * lightLevel;
           gl_FragColor.r = min(gl_FragColor.r + waveOut.r, 1.0);
           gl_FragColor.g = min(gl_FragColor.g + waveOut.g, 1.0);
           gl_FragColor.b = min(gl_FragColor.b + waveOut.b, 1.0);
       }
       
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
