#version 330 core

layout(location = 0) in uint vert_data;

// Uniforms for view and projection matrices, all block colors
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform ivec2 worldPos;
uniform vec3 lightDir;

out vec3 fragColor;
out vec2 fragUV;

vec3 block_colors[3] = vec3[](
    vec3(0.0, 0.0, 0.0), // Block type 0: Default (black or unused)
    vec3(0.55, 0.27, 0.07), // Block type 1: Dirt (brown)
    vec3(0.13, 0.55, 0.13)  // Block type 2: Grass (green)
);

vec3 normals[6] = vec3[6](
    vec3(-1.0, 0.0, 0.0), // Left
    vec3(1.0, 0.0, 0.0),  // Right
    vec3(0.0, -1.0, 0.0), // Down
    vec3(0.0, 1.0, 0.0),  // Up
    vec3(0.0, 0.0, -1.0), // Forward
    vec3(0.0, 0.0, 1.0)   // Back
);

uint x_positive_bits(uint bits) {
    return (1u << bits) - 1u;
}

vec3 calculateLighting(vec3 normal) {
    // Assume lightDir is normalized
    float lightIntensity = max(dot(normal, -lightDir), 0.1); // Ensure it doesn’t go too dark
    return lightIntensity * vec3(1.0); // Adjust the multiplier for light intensity if needed
}

void main() {
    // Extract data from vertex data bit encoding
    float x = float(vert_data & x_positive_bits(6u)); // Extract 6 bits for x
    float y = float((vert_data >> 6u) & x_positive_bits(8u)); // Extract 8 bits for y
    float z = float((vert_data >> 14u) & x_positive_bits(6u)); // Extract 6 bits for z
    uint ao = (vert_data >> 20u) & x_positive_bits(3u); // Extract 3 bits for AO
    uint normal_index = (vert_data >> 23u) & x_positive_bits(4u); // Extract 4 bits for normal
    uint block_type = (vert_data >> 27u) & x_positive_bits(4u); // Extract 4 bits for block type

    vec4 local_position = vec4(x, y, z, 1.0);
    vec4 worldPosition = vec4(worldPos.x, 0.0, worldPos.y, 0.0);
    gl_Position = projectionMatrix * viewMatrix * (worldPosition + local_position);

    if (block_type == 1u) {
        fragColor = block_colors[1]; // Dirt
    } else if (block_type == 2u) {
        fragColor = block_colors[2]; // Grass
    } else if (block_type == 0u) {
        fragColor = vec3(1.0, 1.0, 0.5); // Yellow for unused
    } else {
        fragColor = vec3(1.0, 1.0, 1.0); // Default color (white) for other blocks
    }

    vec3 normal = normals[normal_index];
    vec3 lighting = calculateLighting(normal);
    fragColor *= lighting;
    fragUV = vec2(x, z);
}
