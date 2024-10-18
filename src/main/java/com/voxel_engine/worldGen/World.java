package com.voxel_engine.worldGen;
import com.voxel_engine.Driver;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.render.Renderer;
import org.joml.Vector2i;
import com.voxel_engine.worldGen.chunk.ChunkData;
import com.voxel_engine.worldGen.chunk.ChunkManager;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.worldGen.chunk.ChunkRefs;

import java.util.HashMap;

public class World {
    private HashMap<Vector2i, ChunkData> chunkList;  // Store chunks in a HashMap for easy access
    private ChunkManager chunkManager;

    public World(Renderer renderer, ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
        this.chunkList = new HashMap<>();  // Initialize the world data map

        // Step 1: Generate and store all 9 chunks in the world data map
        generateChunks();

        // Step 2: Create and render meshes for all 9 chunks with proper references
        renderAllChunks();
    }

    // Method to generate all chunks in the 3x3 grid
    private void generateChunks() {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Vector2i chunkPosition = new Vector2i(x * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);
                ChunkData chunkData = new ChunkData(chunkPosition.x, 0, chunkPosition.y);
                chunkList.put(chunkPosition, chunkData);  // Store the chunk data
            }
        }
    }

    // Method to render all chunks with proper references
    private void renderAllChunks() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Vector2i chunkPosition = new Vector2i(x * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);

                // Step 3: Create ChunkRefs for the current chunk
                ChunkRefs chunkRefs = createChunkRefs(chunkPosition);

                // Step 4: Generate mesh for the chunk
                ChunkMesh chunkMesh = Driver.greedyMesher.buildChunkMesh(chunkRefs);

                // Add the chunk mesh to the chunk manager for rendering
                chunkManager.addChunk(chunkPosition, chunkMesh);
            }
        }
    }

    // Helper method to create ChunkRefs for a specific chunk
    private ChunkRefs createChunkRefs(Vector2i centerChunkPos) {
        // Create a map to hold references to the center chunk and its 8 neighbors
        HashMap<Vector2i, ChunkData> references = new HashMap<>();

        // Define relative positions for all 9 chunks (center + 8 neighbors)
        int[] dx = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
        int[] dz = {-1, -1, -1, 0, 0, 0, 1, 1, 1};

        // Get the center chunk and all its neighbors
        for (int i = 0; i < 9; i++) {
            Vector2i pos = new Vector2i(centerChunkPos.x + dx[i] * Constants.CHUNK_SIZE,
                    centerChunkPos.y + dz[i] * Constants.CHUNK_SIZE);

            ChunkData chunkData = chunkList.get(pos);
            if (chunkData != null) {
                references.put(pos, chunkData);
            }
        }

        // Return ChunkRefs using the references map
        return ChunkRefs.tryNew(references, centerChunkPos);
    }
}
