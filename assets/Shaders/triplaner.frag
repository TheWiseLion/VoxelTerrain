uniform sampler2D m_ColorMap1;
uniform sampler2D m_ColorMap2;

varying vec3 normal;
varying float NdotL;
varying vec4 vertex;

void main()
{
    //Triplaner time. Pulled directly from http://http.developer.nvidia.com/GPUGems3/ 
    vec3 blending = (abs( normal ) - 0.1) * 2.0;
    blending = normalize(max(blending, 0));      // Force weights to sum to 1.0 (very important!)
    blending /= (blending.x + blending.y + blending.z );
    vec4 coords = vertex * 0.5;
    

    vec4 col1 = texture2D( m_ColorMap2, coords.yz );//x dom
    vec4 col2; //y dom
    if(normal.y > 0){
        col2 = texture2D( m_ColorMap1, coords.zx ); //This is what gives us the grass on top effect.
    }else{
        col2 = texture2D( m_ColorMap2, coords.zx );
    }
    vec4 col3 = texture2D( m_ColorMap2, coords.xy );//z dom
   

    vec4 colBlended = (col1 * blending.x + col2 * blending.y + col3 * blending.z)*.5f;//diffuse
    gl_FragColor =  colBlended;
    
}