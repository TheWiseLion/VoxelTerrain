uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_NormalMatrix;
uniform mat3 g_ViewMatrix;
uniform vec4 g_LightPosition;

attribute vec3 inPosition;
attribute vec3 inNormal;
varying vec3 normal;
varying float NdotL;
varying vec4 vertex;

void main() {

    vertex = vec4(inPosition,1);
    normal = inNormal;
    vec3 lightDir = normalize(g_LightPosition.xyz);

    /* Compute the diffuse term */
   NdotL = max(dot(normal, lightDir), NdotL);
    
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}
