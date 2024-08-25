#version 330 core

in vec4 color;
in vec2 texCoord;
in float texID;

uniform sampler2D u_Textures[32];

// Light types
#define LIGHT_SUN = 0;
#define LIGHT_POINT = 1;

struct LightSource {
    float type;
    float intensity;
    vec2 pos;
    float radius;
};

uniform LightSource[128] u_lightArray;
uniform int u_lightCount; // Current light source count.
uniform vec2 u_sres;  // Current screen resolution.
uniform vec2 u_cameraOffset; // Current camera offset.

// Get the distance between this light and the current texel.
float getDistance(LightSource light) {
    // This should not have to be converted to world space.
    vec2 fragPosInv = vec2(gl_FragCoord.x, u_sres.y - gl_FragCoord.y);
    vec2 pDelta = (fragPosInv - light.pos) + u_cameraOffset;
    return pDelta.x * pDelta.x + pDelta.y * pDelta.y;
}

// Calculate the relative illumination of a light.
float relIllum(LightSource light) {
    // use gl_FragCoord to get current viewport location.
    // Passed-in light locations need to be resolved to screen-space.

    // Sunlight.
    if (light.type == 0) {
        return light.intensity;
    }

    // Point light.
    float dist = getDistance(light);
    return (1 - smoothstep(0, light.radius * light.radius, dist)) * light.intensity;
}

// Illuminate a texture and return the lit texel.
vec4 illuminate(vec4 texel) {
    float maxIllum = 0;

    // Find the highest illumination from the provided lights.
    for (int i = 0; i < u_lightCount; i++) {
        float illum = relIllum(u_lightArray[i]);

        if (illum > maxIllum) {
            maxIllum = illum;
        }
    }

    // No more math required. Set the output color.
    vec3 color = texel.rgb * maxIllum;
    //float desaturation = 0;

    //float avg = (color.r + color.g + color.b) / 3;
    //vec3 dist = color - avg;
    //vec3 outColor = color - (dist * desaturation);
    return vec4(color, texel.a);
}

void main()
{
    vec4 chosenTex; 
    chosenTex = texture(u_Textures[int(texID)], texCoord);
    chosenTex = illuminate(chosenTex);
    gl_FragColor = chosenTex * color;
}
