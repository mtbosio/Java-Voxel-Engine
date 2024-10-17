package worldGen.chunk;
import main.Driver;
import main.Constants;
import worldGen.chunk.Block.BlockType;

import java.util.Random;

public class ChunkData {

    private Block[] blocks;  // Use a fixed-size array to store blocks


    private int worldX, worldY, worldZ;

    public ChunkData(int worldX, int worldY, int worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        blocks = new Block[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE * Constants.CHUNK_HEIGHT];  // Initialize the array with the capacity of the chunk
        createChunk();
    }

    private void createChunk() {
        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                // Loop through the y levels in the chunk
                double height = Driver.terrainGenerator.getHeight(worldX + x, worldZ + z, Constants.CHUNK_SIZE);
                int roundedHeight = (int) Math.round(height);

                for(int y = 0; y < Constants.CHUNK_SIZE; y++){
                    if(y == roundedHeight){
                        blocks[getIndex(x,y,z)] = new Block(BlockType.GRASS);
                    } else if(y < roundedHeight) {
                        blocks[getIndex(x,y,z)] = new Block(BlockType.DIRT);
                    } else {
                        blocks[getIndex(x,y,z)] = new Block(BlockType.AIR);
                    }
                }

            }
        }
    }
    public void carveCave(int startX, int startY, int startZ, int radius) {
        // Example: Use a random walk to create a cave
        Random rand = new Random();
        for (int i = 0; i < 1; i++) { // Adjust the number of caves
            int x = startX + (rand.nextInt(radius * 2) - radius);
            int y = startY + (rand.nextInt(radius * 2) - radius);
            int z = startZ + (rand.nextInt(radius * 2) - radius);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                            blocks[getIndex(x + dx, y + dy, z + dz)] = new Block(BlockType.AIR);  // Replace with air block
                        }
                    }
                }
            }
        }
    }

    public Block getBlockAtPosition(int x, int y, int z) {
        return blocks[getIndex(x, y, z)];
    }

    private int getIndex(int x, int y, int z) {
        return (y * Constants.CHUNK_SIZE * Constants.CHUNK_SIZE) + (z * Constants.CHUNK_SIZE) + x;
    }

    public Block[] getBlocks() {
        return blocks;
    }
    public int getWorldX() {
        return worldX;
    }

    public int getWorldZ() {
        return worldZ;
    }

}
