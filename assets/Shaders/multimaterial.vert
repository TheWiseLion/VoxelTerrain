uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;

//Light terms:
uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;
uniform vec4 g_AmbientLightColor;

//Unpacking Terms:
//uniform int m_vTD[128]; //Packed 128 indexs
//uniform int m_vTN[128]; //Packed 128 indexs
//uniform int m_vTS[128]; //Packed 128 indexs
uniform float m_bS[16];
uniform float m_shine[16];


uniform float m_minnaertL[16];
uniform float m_minnaertP[16];

uniform float m_rot[16];
uniform float m_oS[16];
uniform float m_hl[16];
uniform float m_des[16];



attribute vec3 inPosition;

//Vertex Normal:
attribute vec3 inNormal;

//Simple triplaner:
attribute vec2 inTexCoord;

//Normal map:
attribute vec4 inTangent;

//Voxel types:
attribute vec3 inTexCoord2;

//Vertex Weights 
attribute vec3 inTexCoord3; 


varying vec4 vertex;
varying vec2 texCoord;
varying vec3 vertexWieght;

//Pixel Blending:
//varying vec3 dI;//Diffuse Indexes
//varying vec3 nI;//Normal Indexes
//varying vec3 sI;//Specular Indexes
varying vec3 baseScalers;
varying float shiny;
varying vec3 rotSpeed;

varying vec3 otherS;
varying vec3 desV;
varying vec3 hlV;

varying float mP; //minnaert parameter
varying float mL; //minnaert limit

varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 SpecularSum;
varying vec3 lightVec; //pos

//Normal map:
//varying vec3 vNormal;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 vType;

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
	vertexWieght = inTexCoord3;
	vertex = vec4(inPosition,1);
	vType = inTexCoord2;
	texCoord = inTexCoord;
    
    //Get things based off uniforms:
    //dI = vec3(3,3,3);
    //dI =  vec3(m_vTD[int(vType.x)],m_vTD[int(vType.y)],m_vTD[int(vType.z)]);
    baseScalers = vec3(m_bS[int(vType.x)],m_bS[int(vType.y)],m_bS[int(vType.z)]);
    shiny = (m_shine[int(vType.x)]+m_shine[int(vType.y)]+m_shine[int(vType.z)])/3.0;
    
    otherS = vec3(m_oS[int(vType.x)],m_oS[int(vType.y)],m_oS[int(vType.z)]);
    hlV = vec3(m_hl[int(vType.x)],m_hl[int(vType.y)],m_hl[int(vType.z)]);
    desV = vec3(m_des[int(vType.x)],m_des[int(vType.y)],m_des[int(vType.z)]);
    rotSpeed = vec3(m_rot[int(vType.x)],m_rot[int(vType.y)],m_rot[int(vType.z)]);
    
    mP = (m_minnaertP[int(vType.x)]+m_minnaertP[int(vType.y)]+m_minnaertP[int(vType.z)])/3.0;
    mL = (m_minnaertL[int(vType.x)]+m_minnaertL[int(vType.y)]+m_minnaertL[int(vType.z)])/3.0;
    
    vec4 modelSpacePos = vec4(inPosition, 1.0);
	vec3 modelSpaceNorm = inNormal;
	vec3 modelSpaceTan  = inTangent.xyz;
   
	vec3 wvPosition = (g_WorldViewMatrix * modelSpacePos).xyz;
	vec3 wvNormal  = normalize(g_NormalMatrix * modelSpaceNorm);
	vec3 viewDir = normalize(-wvPosition);
   
	//Grab Light information:
	vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
	wvLightPos.w = g_LightPosition.w;
	vec4 lightColor = g_LightColor;

   
	//Normalize tangent:
	vec3 wvTangent = normalize(g_NormalMatrix * modelSpaceTan);
   
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
