#version 330 core

layout(location = 0) in uint packedData;

// Uniforms for view and projection matrices, all block colors
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 worldPos;
uniform vec3 lightDir;

out vec3 fragColor;
out vec2 fragUV;

// quad facing +y direction
const vec3 quadVertices[4] = vec3[](
    vec3(0, 0, 0),   // Bottom-left (0, 0, 0)
    vec3(1, 0, 0),   // Bottom-right (1, 0, 0)
    vec3(1, 0, 1),   // Top-right (1, 0, 1)
    vec3(0, 0, 1)    // Top-left (0, 0, 1)
);


vec3 block_colors[3] = vec3[](
    vec3(0.0, 0.0, 0.0), // Block type 0: Default (black or unused)
    vec3(0.55, 0.27, 0.07), // Block type 1: Dirt (brown)
    vec3(0.13, 0.55, 0.13)  // Block type 2: Grass (green)
);

vec3 normals[6] = vec3[](
    vec3(-1.0, 0.0, 0.0), // Left -x
    vec3(1.0, 0.0, 0.0),  // Right +x
    vec3(0.0, -1.0, 0.0), // Down -y
    vec3(0.0, 1.0, 0.0),  // Up +y
    vec3(0.0, 0.0, 1.0),  // Forward +z (corrected)
    vec3(0.0, 0.0, -1.0)  // Back -z (corrected)
);

vec3 calculateLighting(vec3 normal) {
    // Assume lightDir is normalized
    float lightIntensity = max(dot(normal, -lightDir), 0.1); // Ensure it doesn’t go too dark
    return lightIntensity * vec3(1.0); // Adjust the multiplier for light intensity if needed
}

void main() {
    // Extract data from vertex data bit encoding
    float x = float((packedData >> 28u) & 0xFu); // Extract 4 bits for x
    float y = float((packedData >> 24u) & 0xFu); // Extract 4 bits for y
    float z = float((packedData >> 20u) & 0xFu); // Extract 4 bits for z
    int normal_index = int((packedData >> 17u) & 0x7u); // Extract 3 bits for normal index
    uint block_type = (packedData >> 8u) & 0x1FFu; // Extract 9 bits for block type
    uint width = (packedData >> 4u) & 0xFu;     // 4 bits for width
    uint height = packedData & 0xFu;            // 4 bits for height

    vec3 normal = normals[normal_index];

    // Get the base vertex for the quad
    vec3 baseVertex = quadVertices[gl_VertexID];
    baseVertex = vec3(baseVertex.x * float(width), baseVertex.y * float(height), baseVertex.z);

    vec3 finalPosition = baseVertex;

    // Adjust positioning and rotation based on normal direction

    // Adjust positioning and rotation based on normal direction
    if(normal_index == 0){
        // Normal facing -x
        finalPosition = vec3(baseVertex.y, baseVertex.x, baseVertex.z);
    } else if(normal_index == 1){
        // Normal facing +x
        finalPosition = vec3(-baseVertex.y + 1.0, baseVertex.z, baseVertex.x);
    } else if(normal_index == 2){
        // Normal facing -y
        finalPosition = vec3(baseVertex.z, baseVertex.y, baseVertex.x);
    } else if(normal_index == 3){
        // Normal facing +y
        finalPosition = vec3(baseVertex.x, baseVertex.y + 1.0, baseVertex.z);
    } else if(normal_index == 4){
        // Normal facing +z
        finalPosition = vec3(baseVertex.x, -baseVertex.z + 1.0, baseVertex.y + 1.0);
    } else if(normal_index == 5){
        // Normal facing -z
        finalPosition = vec3(baseVertex.x, baseVertex.z, baseVertex.y);
    }
    // Apply world position offset (e.g., chunk position)
    vec4 world_position = vec4(worldPos.x, worldPos.y, worldPos.z, 0.0);
    vec4 local_position = vec4(x,y,z, 0.0);
    vec4 calculated_position = vec4(finalPosition, 1.0) + world_position + local_position;

    // Output final vertex position
    gl_Position = projectionMatrix * viewMatrix * calculated_position;


    // Set fragment color based on block type
    if (block_type == 1u) {
        fragColor = block_colors[1]; // Dirt
    } else if (block_type == 2u) {
        fragColor = block_colors[2]; // Grass
    } else if (block_type == 0u) {
        fragColor = vec3(1.0, 1.0, 0.5); // Yellow for unused
    } else {
        fragColor = vec3(1.0, 1.0, 1.0);
    }
    // Apply lighting to the fragment color
    vec3 lighting = calculateLighting(normal);
    fragColor *= lighting;

    // Placeholder for UVs (can be adjusted as needed)
    fragUV = vec2(baseVertex.x, baseVertex.y);
}
