package main;

import org.spongepowered.noise.Noise;
import org.spongepowered.noise.module.NoiseModule.*;
import org.spongepowered.noise.module.source.Perlin;

public final class TerrainGenerator {
    private final Perlin perlin;

    public TerrainGenerator() {
        // Create a Perlin noise module
        perlin = new Perlin();
        // Set parameters for the noise (frequency, amplitude, etc.)
        perlin.setFrequency(0.1);
        perlin.setLacunarity(2.0);
        perlin.setPersistence(0.5);
        perlin.setSeed(10);
    }

    public double getNoise(double x, double z) {
        // Generate noise at given coordinates
        return perlin.get(x, 0, z);
    }
}