package com.voxel_engine.worldGen.chunk;

import com.voxel_engine.Driver;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.render.Shader;
import com.voxel_engine.utils.Constants;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChunkManager is responsible for the handling of chunks.
 * It keeps track of chunk data.
 *
 *
 */
public class ChunkManager {
    private static ChunkManager instance;
    private Map<Vector3i, ChunkMesh> currentlyRenderedChunks; // all chunks being rendered
    private Map<Vector3i, ChunkData> chunkDataList; // all chunks that have been initialized
    private ChunkManager() {
        this.currentlyRenderedChunks = new HashMap<>();
        this.chunkDataList = new HashMap<>();
    }

    public static ChunkManager getInstance() {
        if (instance == null) {
            instance = new ChunkManager();
        }
        return instance;
    }

    public void addChunkToBeRendered(Vector3i chunkPos, ChunkMesh chunkMesh) {
        currentlyRenderedChunks.put(chunkPos, chunkMesh);
    }
    public void addChunkToChunkList(Vector3i chunkPos, ChunkData chunkData) {
        chunkDataList.put(chunkPos, chunkData);
    }

    public Block getBlockAtWorldPosition(int x, int y, int z) {
        // Adjust chunk coordinates to handle negative values correctly
        int chunkX = (x < 0) ? (((x + 1) / Constants.CHUNK_SIZE) - 1) * 16 : (x / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkY = (y < 0) ? (((y + 1) / Constants.CHUNK_SIZE) - 1) * 16 : (y / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkZ = (z < 0) ? (((z + 1) / Constants.CHUNK_SIZE) - 1) * 16 : (z / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;

        Vector3i key = new Vector3i(chunkX, chunkY, chunkZ);

        ChunkData localChunk = chunkDataList.get(key);
        if (localChunk == null) {
            //System.out.println(x + " " + y + " " + z);
            //System.out.println(chunkX + " " + chunkY + " " + chunkZ);

            return Block.AIR;
        }

        // Use adjusted coordinates to ensure positive local values within the chunk
        int localX = ((x % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localY = ((y % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localZ = ((z % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;

        return localChunk.getBlockAtPosition(localX, localY, localZ);
    }


    // get neighbors [back, forward, down, up, left, right]
    public List<Block> getBlockNeighbors(int x, int y, int z){
        List<Block> result = new ArrayList<>();
        result.add(getBlockAtWorldPosition(x, y, z-1)); // Back
        result.add(getBlockAtWorldPosition(x, y, z+1));  // Forward
        result.add(getBlockAtWorldPosition(x, y-1, z)); // Down
        result.add(getBlockAtWorldPosition(x, y+1, z));  // Up
        result.add(getBlockAtWorldPosition(x-1, y, z)); // Left
        result.add(getBlockAtWorldPosition(x+1, y, z));  // Right
        return result;
    }
}
