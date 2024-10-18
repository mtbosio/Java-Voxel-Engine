package main;

public class TerrainGenerator {
    private final FastNoiseLite noise;
    private final int octaves;
    private final double persistence; // Controls how much each octave contributes
    private final double scale; // Scaling factor for the noise

    public TerrainGenerator(int seed, int octaves, double persistence, double scale) {
        this.noise = new FastNoiseLite();
        noise.SetSeed(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2); // You can choose the noise type
        this.octaves = octaves;
        this.persistence = persistence;
        this.scale = scale;
    }

    // A method to generate height at (x, z) coordinates
    public double getHeight(double x, double z, double maxY) {
        double amplitude = 1;
        double frequency = 1;
        double noiseHeight = 0;

        // Calculate the maximum possible contribution of all remaining octaves
        double maxAmplitude = 0;
        for (int i = 0; i < octaves; i++) {
            maxAmplitude += amplitude;
            amplitude *= persistence;
        }

        amplitude = 1; // Reset amplitude for actual calculation

        // Start adding noise layers
        for (int i = 0; i < octaves; i++) {
            // Generate noise value for the current octave using FastNoiseLite
            double noiseValue = noise.GetNoise((float) (x * frequency * scale), (float) (z * frequency * scale)) * amplitude;

            // Add to the cumulative noise height
            noiseHeight += noiseValue;

            // Calculate maximum possible value of remaining octaves
            maxAmplitude -= amplitude;

            // Check if further calculation is needed
            if (Math.abs(noiseHeight) > maxAmplitude) {
                break; // Skip remaining octaves
            }

            // Update amplitude and frequency for the next octave
            amplitude *= persistence;
            frequency *= 2;
        }

        // Ensure the noiseHeight stays within the maxY limit
        double height = Math.max(0, Math.min(noiseHeight * Constants.CHUNK_HEIGHT, maxY));
        return height;
    }
}