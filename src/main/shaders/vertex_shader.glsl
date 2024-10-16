#version 330 core

layout(location = 0) in uint vert_data;

// Uniforms for view, and projection matrices
// all block colors
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 fragColor;
//uniform vec3 normals[6];

vec3 block_colors[3] = vec3[](
    vec3(0.0, 0.0, 0.0), // Block type 0: Default (black or unused)
    vec3(0.55, 0.27, 0.07), // Block type 1: Dirt (brown)
    vec3(0.13, 0.55, 0.13)  // Block type 2: Grass (green)
);

uint x_positive_bits(uint bits) {
    return (1u << bits) - 1u;
}

void main() {
    // Extract data from vertex data bit encoding
    float x = float(vert_data & x_positive_bits(6u)); // Extract 6 bits for x
    float y = float((vert_data >> 6u) & x_positive_bits(6u)); // Extract 6 bits for y
    float z = float((vert_data >> 12u) & x_positive_bits(6u)); // Extract 6 bits for z
    uint ao = (vert_data >> 18u) & x_positive_bits(3u); // Extract 3 bits for AO
    uint normal = (vert_data >> 21u) & x_positive_bits(4u); // Extract 4 bits for normal
    uint block_type = (vert_data >> 25u) & x_positive_bits(4u); // Extract 4 bits for block type

    vec4 local_position = vec4(x, y, z, 1.0);
    gl_Position = projectionMatrix * viewMatrix * local_position;

    if (block_type == 1u) {
        fragColor = block_colors[1]; // Dirt
    } else if (block_type == 2u) {
        fragColor = block_colors[2]; // Grass
    } else if (block_type == 0u){
        fragColor = vec3(1.0, 1.0, 0.5);
    }
    else {
        fragColor = vec3(1.0, 1.0, 1.0); // Default color (white) for other blocks
    }
}
