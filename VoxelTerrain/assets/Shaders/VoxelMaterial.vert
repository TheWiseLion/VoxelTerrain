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


out vec3 vertex;
out vec3 triplanerNormal;

out vec3 tang;
out vec3 wvNormal;
out vec3 vViewDir;
out vec4 vLightDir;
out vec3 lightVec;

out vec3 AmbientSum;
out vec4 DiffuseSum;
out vec3 SpecularSum;

// JME3 lights in world space
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    lightVec = tempVec;  
    //#ifdef ATTENUATION
     //float dist = length(tempVec);
    // lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
     //lightDir.xyz = tempVec / vec3(dist);
    //#else
     lightDir = vec4(normalize(tempVec), 1.0);
   // #endif
}


void main() {
	triplanerNormal = inNormal;
	vertex = inPosition;
	
	vec4 modelSpacePos = vec4(inPosition, 1.0);
	vec3 modelSpaceNorm = inNormal;
	vec3 modelSpaceTan  = inTangent.xyz;
   
	vec3 wvPosition = (g_WorldViewMatrix * modelSpacePos).xyz;
	wvNormal  = normalize(g_NormalMatrix * modelSpaceNorm);
	vec3 viewDir = normalize(-wvPosition);
   
	//Grab Light information:
	vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
	wvLightPos.w = g_LightPosition.w;
	vec4 lightColor = g_LightColor;
	
	
	
   //Normalize tangent:
	vec3 wvTangent = normalize(g_NormalMatrix * modelSpaceTan);
    tang =modelSpaceTan;
	//Compute bitangent
	vec3 wvBinormal = cross(wvNormal, wvTangent);

	//Make and use TBN matrix:
	mat3 tbnMat = mat3(wvTangent, wvBinormal * inTangent.w,wvNormal);
	
	vViewDir  = -wvPosition * tbnMat;
	//Set light dir + light pos
	lightComputeDir(wvPosition, lightColor, wvLightPos, vLightDir);
	vLightDir.xyz = (vLightDir.xyz * tbnMat).xyz;
   
	lightColor.w = 1.0;
	AmbientSum  = vec3(0.2, 0.2, 0.2) * g_AmbientLightColor.rgb; // Default: ambient color is dark gray
	DiffuseSum  = lightColor;
   
	gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
}