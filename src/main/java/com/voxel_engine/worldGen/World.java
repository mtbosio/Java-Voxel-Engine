package com.voxel_engine.worldGen;
import com.voxel_engine.Driver;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.render.Renderer;
import com.voxel_engine.worldGen.culledMesher.CulledMesher;
import org.joml.Vector2i;
import com.voxel_engine.worldGen.chunk.ChunkData;
import com.voxel_engine.worldGen.chunk.ChunkManager;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.worldGen.chunk.ChunkRefs;
import org.joml.Vector3i;

import java.util.HashMap;

public class World {
    private HashMap<Vector3i, ChunkData> chunkList;  // Store chunks in a HashMap for easy access
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
        for (int x = -6; x <= 6; x++) {
            for (int y = 0; y <= 12; y++) {
                for (int z = -6; z <= 6; z++) {
                    Vector3i chunkPosition = new Vector3i(x * Constants.CHUNK_SIZE, y * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);
                    ChunkData chunkData = new ChunkData(chunkPosition.x, chunkPosition.y, chunkPosition.z);
                    chunkList.put(chunkPosition, chunkData);
                    chunkManager.addChunkToChunkList(chunkPosition, chunkData);
                }
            }
        }
    }

    // Method to render all chunks with proper references
    private void renderAllChunks() {
        for (int x = -6; x <= 6; x++) {
            for (int y = 0; y <= 12; y++) {
                for (int z = -6; z <= 6; z++) {
                    Vector3i chunkPosition = new Vector3i(x * Constants.CHUNK_SIZE, y * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);

                    ChunkData chunkData = chunkList.get(chunkPosition);
                }

            }
        }
    }
}
