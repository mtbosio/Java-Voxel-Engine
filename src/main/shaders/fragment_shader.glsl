#version 330 core

in vec3 fragColor;
out vec4 color;
in vec2 fragUV;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    float noise;
    float modifier;
    color = vec4(fragColor, 1.0);
    noise = random(fragUV * 0.1); // Adjust for scaling
    modifier = 0.8 + (0.4 * noise); // Slightly change color intensity

    // Combine base color with noise modifier
    color *= modifier;
}
