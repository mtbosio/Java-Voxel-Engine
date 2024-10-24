package com.voxel_engine.worldGen.chunk;
import com.voxel_engine.Driver;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.worldGen.chunk.Block;
import java.util.Random;

public class ChunkData {

    private Block[] blocks;  // Use a fixed-size array to store blocks


    private int worldX, worldY, worldZ;

    public ChunkData(int worldX, int worldY, int worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        blocks = new Block[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];  // Initialize the array with the capacity of the chunk
        createChunk();
    }

    private void createChunk() {
        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                // Loop through the y levels in the chunk
                double height = Driver.terrainGenerator.getHeight(worldX + x, worldZ + z);
                int roundedHeight = (int) Math.round(height);

                for(int y = 0; y < Constants.CHUNK_SIZE; y++){
                    if(worldY + y == roundedHeight){
                        blocks[getIndex(x,y,z)] = Block.GRASS;
                    } else if(worldY + y < roundedHeight) {
                        blocks[getIndex(x,y,z)] = Block.DIRT;
                    } else {
                        blocks[getIndex(x,y,z)] = Block.AIR;
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
    public int getWorldY() {
        return worldY;
    }
    public int getWorldZ() {
        return worldZ;
    }
}
