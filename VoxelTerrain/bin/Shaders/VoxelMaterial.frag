#extension GL_EXT_texture_array : enable

uniform sampler2DArray m_colorMaps;
uniform int m_colorIndex;
uniform vec4 g_LightPosition;

in vec3 vertex;
in vec3 triplanerNormal;
in vec3 wvNormal;
in vec3 AmbientSum;
in vec4 DiffuseSum;
in vec3 SpecularSum;

//Pixel Lighting:
uniform vec4 g_LightDirection;
in vec3 vViewDir;
in vec4 vLightDir;
in vec3 lightVec;

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
	float ld = max(0.0, dot(norm, lightdir));
	//float NdotL = max(0.0, dot(norm, lightdir));
    //float NdotV = max(0.0, dot(norm, viewdir));
    //ld = NdotL * pow(max(NdotL * NdotV,.1), -1.0);
    
	return ld;
    
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shine){
    // Standard Phong
	vec3 R = reflect(-lightdir, norm);
	return pow(max(dot(R, viewdir), 0.0), shine);
    
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir,float s){
	float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
	float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, s);

	float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
  
	if (s <= 1.0) {
		specularFactor = 0.0; // should be one instruction on most cards ..
	}

	specularFactor *= diffuseFactor;

	return vec2(diffuseFactor, specularFactor)* vec2(att);
}


void main(){
	//Preform Triplaner:
    vec3 blending = abs( triplanerNormal );
    blending = (blending - 0.2) * 0.7;
    blending = normalize(max(blending, 0.00001));
    blending.xyz /= (blending.x + blending.y + blending.z );
	
	//TODO: Make this a function
	float scalar = 1.0;
	vec3 coords = vertex;
    vec4 col1 = texture2DArray( m_colorMaps, vec3(coords.yz * scalar,m_colorIndex));
    vec4 col2 = texture2DArray( m_colorMaps,  vec3(coords.xz * scalar,m_colorIndex));
    vec4 col3 = texture2DArray( m_colorMaps, vec3(coords.xy * scalar,m_colorIndex));
    // blend the results of the 3 planar projections.
    vec4 finalColor = col1 * blending.x + col2 * blending.y + col3 * blending.z;
	
	
	//Spot Fall Off 
	float spotFallOff = .25;
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
    
	//Lighting Terms
	vec4 lightDir = vLightDir;
	lightDir.xyz = normalize(lightDir.xyz);
	vec3 viewDir = normalize(vViewDir);

	vec2 light = computeLighting(wvNormal, viewDir, lightDir.xyz,1.0) * spotFallOff;
	
	gl_FragColor.rgb =  AmbientSum * finalColor.rgb  +
						DiffuseSum.rgb * finalColor.rgb  * vec3(light.x) +
						vec3(light.y);
	gl_FragColor.rgb = finalColor.rgb;
	gl_FragColor.a = 1.0;
}