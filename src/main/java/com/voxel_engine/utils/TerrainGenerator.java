package com.voxel_engine.utils;

public class TerrainGenerator {
    private static TerrainGenerator instance;
    private final FastNoiseLite noise;
    private final FastNoiseLite mountainNoise;
    private final FastNoiseLite hillNoise;

    private TerrainGenerator(int seed) {
        this.noise = new FastNoiseLite();
        this.mountainNoise = new FastNoiseLite();
        this.hillNoise = new FastNoiseLite();

        noise.SetSeed(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFrequency(0.01f);

        mountainNoise.SetSeed(seed + 1);
        mountainNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
        mountainNoise.SetFrequency(0.02f);

        hillNoise.SetSeed(seed + 2);
        hillNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        hillNoise.SetFrequency(0.005f);
    }

    public int getHeight(int worldX, int worldZ) {
        // Blend two noise types
        float mountainHeight = mountainNoise.GetNoise(worldX, worldZ) * 0.2f;
        float hillHeight = hillNoise.GetNoise(worldX, worldZ) * 0.03f;

        float combinedHeight = mountainHeight + hillHeight;
        int height = (int) ((combinedHeight + 1) * 127.5f);

        return Math.max(0, Math.min(height, 255));
    }

    public static TerrainGenerator getInstance() {
        if (instance == null) {
            instance = new TerrainGenerator(24567);
        }
        return instance;
    }

}