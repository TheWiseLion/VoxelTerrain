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
in vec3 viewDir;
in vec3 lightVec;
in vec3 halfVec;

out vec3 AmbientSum;
out vec4 DiffuseSum;
out vec3 SpecularSum;

void main(){
	//Preform Triplaner:
    vec3 blending = abs( triplanerNormal );
    blending = (blending - 0.2) * 0.7;
    blending = normalize(max(blending, 0.00001));
    blending.xyz /= (blending.x + blending.y + blending.z );
	
	vec3 coords = vertex * m_Scalar;
	#ifdef DIFFUSEMAP
		vec4 c1 = texture2D(m_DiffuseMap, coords.yz);
		vec4 c2 = texture2D(m_DiffuseMap, coords.xz);
		vec4 c3 = texture2D(m_DiffuseMap, coords.xy);
		vec4 diffuseColor = c1 * blending.x + c2 * blending.y + c3 * blending.z;
    #else
		vec4 diffuseColor = vec4(1.0);
    #endif
	
	//#ifdef NORMALMAP
		vec4 n1 = texture2D(m_NormalMap, coords.yz);
		vec4 n2 = texture2D(m_NormalMap, coords.xz);
		vec4 n3 = texture2D(m_NormalMap, coords.xy);
    	vec4 normalHeight = n1 * blending.x + n2 * blending.y + n3 * blending.z;
    	vec3 normal = normalize((normalHeight.xyz * vec3(2.0) - vec3(1.0)));
	//#else
	// 	vec3 normal = normalize(triplanerNormal);
	//#endif
	
	//#ifdef SPECULARMAP
	//	vec4 s1 = texture2D(m_SpecularMap, coords.yz);
	//	vec4 s2 = texture2D(m_SpecularMap, coords.xz);
	//	vec4 s3 = texture2D(m_SpecularMap, coords.xy);
	//	vec4 specularColor = s1 * blending.x + s2 * blending.y + s3 * blending.z;
   // #else
		vec4 specularColor = vec4(1.0);
   // #endif
	
	float lamberFactor = max (dot (lightDir, normal), 0.0) * max (dot (lightDir, triplanerNormal), 0.0) ;
	
    //gl_FragColor.rgb =  AmbientSum       * diffuseColor.rgb  +
    //                    DiffuseSum.rgb   * diffuseColor.rgb  * vec3(light.x) +
    //                    SpecularSum2.rgb * specularColor.rgb * vec3(light.y);
    gl_FragColor.rgb = diffuseColor * max(lamberFactor, .3) * 1.5;
	gl_FragColor.a = 1.0;
	
}