package render.shaders

fun fireFragShader() = """
    uniform float u_Time;
    
    varying vec2 UV;
    varying float v_Offset;
    
        
        vec2 hash( in vec2 x ) {
            const vec2 k = vec2(0.3183099, 0.3678794);
            x = x * k + k.yx;
            return -1.0 + 2.0 * fract( 16.0 * k * fract(x.x * x.y * (x.x + x.y)));
        }
        
        vec3 calcNoise( in vec2 p ) {
            vec2 i = floor( p );
            vec2 f = fract( p );
            vec2 u = f*f*(3.0-2.0*f);
            vec2 du = 6.0*f*(1.0-f);
            vec2 ga = hash( i + vec2(0.0,0.0) );
            vec2 gb = hash( i + vec2(1.0,0.0) );
            vec2 gc = hash( i + vec2(0.0,1.0) );
            vec2 gd = hash( i + vec2(1.0,1.0) );
            
            float va = dot( ga, f - vec2(0.0,0.0) );
            float vb = dot( gb, f - vec2(1.0,0.0) );
            float vc = dot( gc, f - vec2(0.0,1.0) );
            float vd = dot( gd, f - vec2(1.0,1.0) );

            return vec3( va + u.x*(vb-va) + u.y*(vc-va) + u.x*u.y*(va-vb-vc+vd),
                         ga + u.x*(gb-ga) + u.y*(gc-ga) + u.x*u.y*(ga-gb-gc+gd) +
                         du * (u.yx*(va-vb-vc+vd) + vec2(vb,vc) - va));
        }
        
        float sdfCircle(vec2 p, float r) {
          return length(p) - r;
        }
        
        
    void main()
    {
        float octaves = 8.0;
        float timeMultiple = 0.01;
        float noiseAmount = calcNoise(vec2(octaves * UV.x, (octaves * UV.y) + ((u_Time + 4.2 + v_Offset) * timeMultiple))).x;
        float yGradient = clamp(0.7 - UV.y, 0.0, 1.0) * 0.6;
        vec2 sdfNoise = vec2(noiseAmount * 0.1, noiseAmount * 2.5 * yGradient);
        
        vec2 p1 = (UV - vec2(0.5, 0.7)) + sdfNoise;
        vec2 p2 = (UV - vec2(0.5, 0.775)) + sdfNoise;
        vec2 p3 = (UV - vec2(0.5, 0.8)) + sdfNoise;
        
        float amountOuter = step(sdfCircle(p1, 0.25), 0.0);
        float amountCenter = step(sdfCircle(p2, 0.175), 0.0);
        float amountInner = step(sdfCircle(p3, 0.1), 0.0);
        
        vec3 outer = vec3(1.0, 0.0, 0.0) * amountOuter;
        vec3 center = vec3(1.0, 0.5, 0.0) * amountCenter;
        vec3 inner = vec3(1.0, 1.0, 0.3) * amountInner;
        
        gl_FragColor = vec4(outer + inner + center, amountOuter * 0.6);
    }

""".trimIndent()
