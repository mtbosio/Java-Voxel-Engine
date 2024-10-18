package com.voxel_engine.worldGen.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.voxel_engine.utils.Constants;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class ChunkRefs {


    private List<ChunkData> chunks;

    public ChunkRefs(List<ChunkData> chunks) {
        this.chunks = new ArrayList<>(chunks);
    }

    // Create a ChunksRefs instance centered around a specified chunk position
    public static ChunkRefs tryNew(HashMap<Vector2i, ChunkData> worldData, Vector2i middleChunk) {
        List<ChunkData> chunkList = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                    Vector2i offset = new Vector2i(x, z);
                    Vector2i chunkPosition = new Vector2i(middleChunk).add(offset.mul(32)); // Assuming chunk size is 16
                    ChunkData chunkData = worldData.get(chunkPosition);
                    if (chunkData == null) {
                        throw new IllegalArgumentException("ChunkData doesn't exist in the input world data");
                    }
                    chunkList.add(chunkData);
            }
        }


        return new ChunkRefs(chunkList);
    }

    // Check if all voxels in the chunks are the same
    public boolean isAllVoxelsSame() {
        Block firstBlock = chunks.get(0).getBlockAtPosition(0, 0, 0); // Getting a block from the first chunk
        if (firstBlock == null) {
            return false;
        }

        for (ChunkData chunk : chunks.subList(1, chunks.size())) {
            Block block = chunk.getBlockAtPosition(0, 0, 0); // Compare with the first block
            if (block == null || block.getBlockType() != firstBlock.getBlockType()) {
                return false;
            }
        }
        return true;
    }

    // Get a block at the specified position relative to the middle chunk
    public Block getBlock(Vector3i pos) {
        // Calculate relative position within the 3x3 area
        int relativeX = pos.x / Constants.CHUNK_SIZE + 1; // This gives us -1, 0, or 1
        int relativeZ = pos.z / Constants.CHUNK_SIZE + 1; // This gives us -1, 0, or 1

        // Calculate chunk index based on relative x and z positions
        int chunkIndex = relativeZ * 3 + relativeX;

        // Check if chunkIndex is within the valid range
        if (chunkIndex < 0 || chunkIndex >= chunks.size()) {
            return null; // Return null if the index is out of bounds
        }

        ChunkData chunkData = chunks.get(chunkIndex);

        // Get the local block position within the chunk
        int localX = pos.x % Constants.CHUNK_SIZE; // Local x in chunk
        int localY = pos.y % Constants.CHUNK_HEIGHT; // Local y in chunk
        int localZ = pos.z % Constants.CHUNK_SIZE; // Local z in chunk

        // Ensure local positions are non-negative
        if (localX < 0) localX += Constants.CHUNK_SIZE;
        if (localY < 0) localY += Constants.CHUNK_HEIGHT;
        if (localZ < 0) localZ += Constants.CHUNK_SIZE;


        return chunkData.getBlockAtPosition(localX, localY, localZ);
    }

    // Get a block without considering neighbors (from the center chunk)
    public Block getBlockNoNeighbor(Vector3i pos) {
        ChunkData chunkData = chunks.get(5); // Assuming the center chunk is at index 5
        return chunkData.getBlockAtPosition(pos.x, pos.y, pos.z);
    }

    // Sample adjacent blocks (back, left, down)
    public Block[] getAdjacentBlocks(Vector3i pos) {
        Block current = getBlock(pos);
        Block back = getBlock(new Vector3i(pos).add(0, 0, -1));
        Block left = getBlock(new Vector3i(pos).add(-1, 0, 0));
        Block down = getBlock(new Vector3i(pos).add(0, -1, 0));
        return new Block[]{current, back, left, down};
    }

    // Sample blocks in all six directions (Von Neumann neighborhood)
    public List<Block> getVonNeumann(Vector3i pos) {
        List<Block> result = new ArrayList<>();
        result.add(getBlock(new Vector3i(pos).add(0, 0, -1))); // Back
        result.add(getBlock(new Vector3i(pos).add(0, 0, 1)));  // Forward
        result.add(getBlock(new Vector3i(pos).add(0, -1, 0))); // Down
        result.add(getBlock(new Vector3i(pos).add(0, 1, 0)));  // Up
        result.add(getBlock(new Vector3i(pos).add(-1, 0, 0))); // Left
        result.add(getBlock(new Vector3i(pos).add(1, 0, 0)));  // Right
        return result;
    }

    // Helper function to get two blocks based on their positions
    public Block[] get2(Vector3i pos, Vector3i offset) {
        Block first = getBlock(pos);
        Block second = getBlock(new Vector3i(pos).add(offset));
        return new Block[]{first, second};
    }

    public List<ChunkData> getChunks() {
        return chunks;
    }
}
