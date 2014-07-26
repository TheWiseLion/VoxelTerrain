uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform vec4 m_Specular;
uniform float m_Shininess;

//Light terms:
uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;
uniform vec4 g_AmbientLightColor;


//Vertex Position:
attribute vec3 inPosition;

//Vertex Normal:
attribute vec3 inNormal;

//Vertex Tangent:
attribute vec4 inTangent;

//Vertex Position (model space)
out vec3 vertex;

//Normal in model space
out vec3 triplanerNormal;

//Normal in view space
out vec3 vNormal;

out vec3 halfVec;

//Light Information
out vec3 lightVec;
out vec3 viewDir;
out vec3 lightDir;

out vec3 AmbientSum;
out vec4 DiffuseSum;
out vec3 SpecularSum;

// JME3 lights in world space
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
	 
    #ifdef ATTENUATION
    	float posLight = step(0.5, color.w);
		vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
		lightVec = tempVec; 
    
		float dist = length(tempVec);
		lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
		lightDir.xyz = tempVec / vec3(dist);
	#else
		lightDir = normalize(g_LightPosition);
		//lightDir = vec4(normalize(tempVec), 1.0);
	#endif
}

void main() {
	triplanerNormal = inNormal;
	vertex = inPosition;
	
	vec4 modelSpacePos = vec4(inPosition,1.0);
	vec3 modelSpaceNorm = inNormal;
   
	vec3 vPosition = (g_WorldViewMatrix * modelSpacePos).xyz;
	vNormal  = normalize(g_NormalMatrix * modelSpaceNorm);
	vec3 viewDir = normalize(-vPosition);
   
	//Grab Light information:
	//vec4 vLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
	//lightPos.w = g_LightPosition.w;
	vec4 lightColor = g_LightColor;

    //#if defined(NORMALMAP)
    	//Normalize tangent:
		vec3 t = normalize(g_NormalMatrix * inTangent.xyz);
	   	t = normalize(t-vNormal*dot(vNormal,t));
	   	
		//Compute bitangent
		vec3 b = cross(vNormal, t);
	
		//Make and use TBN matrix:
		//mat3 tbnMat = mat3(wvTangent, wvBinormal * inTangent.w, vNormal);
		
		//Set light dir + light pos
		lightDir = g_LightPosition;
		
		vec3 v;
		v.x = dot (lightDir, t);
		v.y = dot (lightDir, b);
		v.z = dot (lightDir, vNormal);
		lightVec = normalize (v);
		
		v.x = dot (vPosition, t);
		v.y = dot (vPosition, b);
		v.z = dot (vPosition, vNormal);
		viewDir = normalize (v);
		
		/* Normalize the halfVector to pass it to the fragment shader */
	
		vec3 p = normalize(vPosition);
		// No need to divide by two, the result is normalized anyway.
		vec3 halfVector = normalize(p + lightDir);
		v.x = dot (halfVector, t);
		v.y = dot (halfVector, b);
		v.z = dot (halfVector, vNormal);
		halfVec = v ;
   //#else !defined(NORMALMAP)
	//    lightDir = g_LightPosition;
   //#endif
 	
   
	lightColor.w = 1.0;
	AmbientSum  = vec3(0.2, 0.2, 0.2) * g_AmbientLightColor.rgb; // Default: ambient color is dark gray
	DiffuseSum  = lightColor;
   
	gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
}