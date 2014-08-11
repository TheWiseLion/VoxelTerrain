#ifdef DIFFUSEMAP
  uniform sampler2D m_DiffuseMap;
#endif

#ifdef SPECULARMAP
  uniform sampler2D m_SpecularMap;
#endif

#ifdef NORMALMAP
  uniform sampler2D m_NormalMap;   
#endif

#ifdef PARALLAXMAP
  uniform sampler2D m_ParallaxMap;  
#endif

#ifdef PARALLAXMAP
    uniform float m_ParallaxHeight;
#endif


uniform float m_Scalar;
uniform float m_Shininess;

//Vertex Position (model space)
in vec3 vertex;

//Normal in model space
in vec3 triplanerNormal;

//Normal in view space
in vec3 vNormal;



//Light Information
in vec3 lightDir;

//Specular or bump info
in vec3 AmbientSum;
in vec4 DiffuseSum;
in vec3 SpecularSum;
in vec3 wvNormal;
//Pixel Lighting:
uniform vec4 g_LightDirection;
in vec3 vViewDir;
in vec4 vLightDir;
in vec3 lightVec;
in vec3 tang;


float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    float ld = max(0.2, dot(norm, lightdir));
    return ld;
    
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shine){
    // Standard Phong
    vec3 R = reflect(-lightdir, norm);
    return pow(max(dot(R, viewdir), 0.0), shine);
    
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir,in float s){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
   float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, s);

   //#ifdef HQ_ATTENUATION
   // float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
  // #else
    float att = vLightDir.w;
  // #endif

   if (s <= 1.0) {
       specularFactor = 0.0; // should be one instruction on most cards ..
   }

   specularFactor *= diffuseFactor;
   return vec2(diffuseFactor, specularFactor) ;
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main(){
	//Preform Triplaner:
    vec3 blending = abs( triplanerNormal );
    blending = (blending - 0.2) * 0.7;
    //blending = step(max(max(blending.x,blending.y),blending.z)-.000001,blending);
    blending = normalize(max(blending, 0.00001));
    blending.xyz /= (blending.x + blending.y + blending.z );
	
	//TODO: obtain dominate 2 coords
	
	
	vec3 coords = vertex * m_Scalar;
	#ifdef DIFFUSEMAP
		vec4 c1 = texture2D(m_DiffuseMap, coords.yz);
		vec4 c2 = texture2D(m_DiffuseMap, coords.xz);
		vec4 c3 = texture2D(m_DiffuseMap, coords.xy);
		vec4 diffuseColor = c1 * blending.x + c2 * blending.y + c3 * blending.z;
    #else
		vec4 diffuseColor = vec4(1.0);
    #endif
	
	#ifdef NORMALMAP
		vec4 n1 = texture2D(m_NormalMap, coords.yz);
		vec4 n2 = texture2D(m_NormalMap, coords.xz);
		vec4 n3 = texture2D(m_NormalMap, coords.xy);
    	vec4 normalHeight = n1 * blending.x + n2 * blending.y + n3 * blending.z;
    	vec3 normal = normalize((normalHeight.xyz * vec3(2.0) - vec3(1.0)));
	#else
	 	vec3 normal = normalize(triplanerNormal);
	#endif
	
	#ifdef SPECULARMAP
		vec4 s1 = texture2D(m_SpecularMap, coords.yz);
		vec4 s2 = texture2D(m_SpecularMap, coords.xz);
		vec4 s3 = texture2D(m_SpecularMap, coords.xy);
		vec4 specularColor = s1 * blending.x + s2 * blending.y + s3 * blending.z;
    #else
		vec4 specularColor = vec4(1.0);
    #endif

	
	//Spot Fall Off 
	float spotFallOff = 1.0;
	if(g_LightDirection.w != 0.0){
		vec3 L       = normalize(lightVec.xyz);
        vec3 spotdir = normalize(g_LightDirection.xyz);
        float curAngleCos = dot(-L, spotdir);             
        float innerAngleCos = floor(g_LightDirection.w) * 0.001;
        float outerAngleCos = fract(g_LightDirection.w);
        float innerMinusOuter = innerAngleCos - outerAngleCos;
        spotFallOff = (curAngleCos - outerAngleCos) / innerMinusOuter;
		if(spotFallOff <= 0.0){
			gl_FragColor.rgb = AmbientSum * diffuseColor.rgb;
			gl_FragColor.a   = 1.0;
			return;
        }else{
         	spotFallOff = clamp(spotFallOff, 0.0, 1.0);
        }
    }
	
	vec4 lightDir = vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
    vec3 viewDir = normalize(vViewDir);
	vec2 light = computeLighting(normal, viewDir, lightDir.xyz,m_Shininess) * spotFallOff;

	vec4 SpecularSum2 = vec4(SpecularSum, 1.0);
 	gl_FragColor.rgb =  AmbientSum       * diffuseColor.rgb  +
                        DiffuseSum.rgb   * diffuseColor.rgb  * vec3(light.x) +
                           SpecularSum2.rgb * specularColor.rgb * vec3(light.y);
    //gl_FragColor.rgb = blending;
    gl_FragColor.a = 1.0;
	
	
	
}