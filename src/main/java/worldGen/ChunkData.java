package worldGen;

import main.Renderer;
import main.Driver;
import main.Constants;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class ChunkData {
    private static int CHUNK_SIZE = Constants.CHUNK_SIZE;

    private Block[] blocks;  // Use a fixed-size array to store blocks
    private float worldX, worldY, worldZ;

    public ChunkData(float worldX, float worldY, float worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        blocks = new Block[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];  // Initialize the array with the capacity of the chunk
        createChunk();
    }

    private void createChunk() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // Use Perlin noise to get ground height
                double noiseValue = Driver.terrainGenerator.getNoise(worldX + x, worldZ + z);
                int height = (int) (noiseValue * 10f);

                // Loop through the y levels in the chunk
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    Block block;
                    if (y < height) {
                        // Set dirt blocks for y values below the terrain height
                        block = new Block(BlockType.DIRT);
                    } else if (y == height) {
                        // Set grass block at the terrain height
                        block = new Block(BlockType.GRASS);
                    } else {
                        // Set air block for y values above the terrain height
                        block = new Block(BlockType.AIR);
                    }

                    // Set the block at the correct index
                    blocks[getIndex(x, y, z)] = block; // Directly assign to the array
                }
            }
        }
    }

    public Block getBlockAtPosition(int x, int y, int z) {
        return blocks[getIndex(x, y, z)];
    }

    private int getIndex(int x, int y, int z) {
        // Calculate the 1D index based on the 3D coordinates
        int x_i = x % 32;
        int y_i = y * 32;
        int z_i = z * (32 * 32);

        return x_i + y_i + z_i;
    }

    public Block[] getBlocks() {
        return blocks;
    }
}
