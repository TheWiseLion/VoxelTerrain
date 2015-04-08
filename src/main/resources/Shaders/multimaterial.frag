uniform sampler2DArray m_materialColors;
uniform sampler2DArray m_materialSpecular;
uniform sampler2DArray m_materialNormal;
uniform sampler2D m_noise;

varying vec4 vertex;
varying vec2 texCoord;
varying vec3 vertexWieght;
varying vec3 vType;

//Rendering Effects:
varying vec3 baseScalers;
varying float shiny;
varying vec3 rotSpeed;

varying vec3 otherS;
varying vec3 desV;
varying vec3 hlV;

varying float mP; //minnaert parameter
varying float mL; //minnaert limit
//

varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 SpecularSum;

//Pixel Lighting:
uniform vec4 g_LightDirection;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;



float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    return d;
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    float ld = max(0.0, dot(norm, lightdir));
    if(mP > 0.0){
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
       ld = NdotL * min(pow(max(NdotL * NdotV*mP,.1), -1.0),mL);
    }
    return ld;
    
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shine){
    // Standard Phong
    vec3 R = reflect(-lightdir, norm);
    return pow(max(tangDot(R, viewdir), 0.0), shine);
    
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir,float s){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
   float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, s);

   //#ifdef HQ_ATTENUATION
   // float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
  // #else
    float att = vLightDir.w;
  // #endif

   if (shiny <= 1.0) {
       specularFactor = 0.0; // should be one instruction on most cards ..
   }

   specularFactor *= diffuseFactor;

   return vec2(diffuseFactor, specularFactor) ;
}

vec3 desaturate(in vec3 color,in float amount)
{
    vec3 gray = vec3(dot(vec3(0.2126,0.7152,0.0722), color));
    //vec3 gray = vec3(dot(vec3(0.3, 0.59, 0.11), color));
    return vec3(mix(color, gray, amount));
}

vec2 rotate(in vec2 coords,in float rotation){
	float c = cos(rotation);
	float s = sin(rotation);
	mat2 r = mat2(c,-s,s,c);
	vec2 rotated = coords - .5f;
	rotated = r*rotated;
	rotated+=.5f;
	return rotated;
}


vec2 rotateCoords(in vec2 coords,in float rotSpeed){
	float r = int((coords.x+coords.y)/rotSpeed)%2;
	float angle = 1.570795f * r;
	return rotate(coords,angle);
}

//vec4 getRotatedSample(vec2 coord, float index){
//	vec4 sample = texture2DArray(m_materialColors,vec3(coord,index));
//	vec4 rotatedSample = texture2DArray(m_materialColors,vec3(rotate(coord,1.570795f),index));
//	float f = texture2D(m_noise,coord*(1.0/64.0)).g;
//	return mix(sample,rotatedSample,f);
//}

//Multi-Scale Sampling
vec4 getColor(vec2 coord, float sc1, float sc2, float hl, float desat,float index){
	vec3 scaler1 = texture2DArray(m_materialColors, vec3(texCoord*sc1,index)).xyz;
	if(sc2 > 0 ){
		vec3 scaler1Desat = desaturate(scaler1*hl,desat);
		vec3 scaler2 = texture2DArray(m_materialColors, vec3(texCoord*sc2,index)).xyz*hl;
		scaler1 = scaler1Desat*scaler2;	
	}
	return vec4(scaler1,1.0);
}

void main()
{
    //Now that we're in frag shader our type wieghts are "averaged"
    //Now we normalize them:
    vec3 weights =abs( vertexWieght / (vertexWieght.x + vertexWieght.y + vertexWieght.z));
    
  	vec3 indexes = vType;
  	//vec2 rot = rotate(texCoord,1.570795f);
    
    ///////////////////////////Type 1////////////////////////////////////////////
    //Texture 1:
    vec2 coord1 = texCoord;
    if(rotSpeed.x > 0){
    	coord1 = rotateCoords(texCoord,rotSpeed.x);
    }
    vec4 tex1 = getColor(coord1,baseScalers.x,otherS.x,hlV.x,desV.x,vType.x);
    vec3 coord1T = vec3(coord1*baseScalers.x,vType.x);
    vec4 n1 = texture2DArray(m_materialNormal, coord1T);
    vec4 s1 = texture2DArray(m_materialSpecular,coord1T);
    ////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////Type 2////////////////////////////////////////////
    //Texture 2:
     vec2 coord2 = texCoord;
    if(rotSpeed.y > 0){
    	coord2 = rotateCoords(texCoord,rotSpeed.y);
    }
    vec4 tex2 = getColor(coord2,baseScalers.y,otherS.y,hlV.y,desV.y,vType.y);
    vec3 coord2T = vec3(coord2*baseScalers.y,vType.y);
    vec4 n2 = texture2DArray(m_materialNormal, coord2T);
    vec4 s2 = texture2DArray(m_materialSpecular,coord2T);
  	/////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////Type 3////////////////////////////////////////////
    //Texture 3:
    vec2 coord3 = texCoord;
    if(rotSpeed.z > 0){
    	coord3 = rotateCoords(texCoord,rotSpeed.z);
    }
    vec4 tex3 = getColor(coord3,baseScalers.z,otherS.z,hlV.z,desV.z,vType.z);
    vec3 coord3T = vec3(coord3*baseScalers.z,vType.z);
    vec4 n3 = texture2DArray(m_materialNormal, coord3T);
    vec4 s3 = texture2DArray(m_materialSpecular,coord3T);
    /////////////////////////////////////////////////////////////////////////////
    
    vec4 finalColor = (tex1 * weights.x) + (tex2 * weights.y) + (tex3 * weights.z);
   	vec4 finalNormal = (n1 * weights.x) + (n2 * weights.y) + (n3 * weights.z);
    vec4 finalSpecular = (s1 * weights.x) + (s2 * weights.y) + (s3 * weights.z);

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
			gl_FragColor.rgb = AmbientSum * finalColor.rgb;
			gl_FragColor.a   = 1.0;
			return;
        }else{
         	spotFallOff = clamp(spotFallOff, 0.0, 1.0);
        }
    }

    
	//From lighting.frag:
    vec3 normal = normalize((finalNormal.xyz * vec3(2.0,-2.0,2.0) - vec3(1.0,-1.0,1.0)));
   
    
    //Final Approach ^.^
    vec4 lightDir = vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
    vec3 viewDir = normalize(vViewDir);
    
    vec2 light;
    if(shiny >=255){
    	light = computeLighting(normal, viewDir, lightDir.xyz,finalSpecular.r * shiny) * spotFallOff;
    	gl_FragColor.rgb =  AmbientSum * finalColor.rgb  +
                        DiffuseSum.rgb   * finalColor.rgb  * vec3(light.x) +
                        vec3(light.y);
    }else{
    	light = computeLighting(normal, viewDir, lightDir.xyz,shiny) * spotFallOff;
    	gl_FragColor.rgb =  AmbientSum * finalColor.rgb  +
                        DiffuseSum.rgb   * finalColor.rgb  * vec3(light.x) +
                         vec3(light.y);
    }
	
    
   
     
    
    // finalColor.rgb*NdotL +
	//gl_FragColor.rgb =  finalColor.rgb*NdotL;//vec4(offsets[0].y,0,0,1);
	gl_FragColor.a = 1.0;
	
}