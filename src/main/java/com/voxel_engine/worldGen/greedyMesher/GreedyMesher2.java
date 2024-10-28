package com.voxel_engine.worldGen.greedyMesher;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.utils.Direction;
import com.voxel_engine.worldGen.chunk.Block;
import com.voxel_engine.worldGen.chunk.ChunkData;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreedyMesher2 {
    public ChunkMesh buildChunkMesh(ChunkData chunkData, Map<Vector3i, ChunkData> chunkNeighbors) {
        List<Integer> instances = new ArrayList<>();
        ChunkMesh chunkMesh = new ChunkMesh(chunkData);

        // Directions for each face: -X, +X, -Y, +Y, -Z, +Z
        Vector3i[] directions = {
                new Vector3i(-1, 0, 0), new Vector3i(1, 0, 0),
                new Vector3i(0, -1, 0), new Vector3i(0, 1, 0),
                new Vector3i(0, 0, -1), new Vector3i(0, 0, 1)
        };

        // Axis mapping for each direction
        int[][] axes = {
                {1, 2}, {1, 2}, // -X, +X -> YZ plane
                {0, 2}, {0, 2}, // -Y, +Y -> XZ plane
                {0, 1}, {0, 1}  // -Z, +Z -> XY plane
        };

        // Process all six face directions
        for (int dir = 0; dir < 6; dir++) {
            Vector3i direction = directions[dir];
            int u = axes[dir][0]; // Primary axis for width
            int v = axes[dir][1]; // Secondary axis for height

            // Visited array to track merged blocks
            boolean[][] visited = new boolean[Constants.CHUNK_SIZE][Constants.CHUNK_SIZE];

            // Iterate over slices of the chunk along the axis
            for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                    // Clear visited
                    for (int i = 0; i < Constants.CHUNK_SIZE; i++) {
                        for (int j = 0; j < Constants.CHUNK_SIZE; j++) {
                            visited[i][j] = false;
                        }
                    }

                    // Iterate through the slice along the current axis
                    for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                        // Get the current block type
                        int blockType = chunkData.getBlockAtPosition(x, y, z).getId();

                        // Determine neighbor position for visibility check
                        Vector3i neighborPos = new Vector3i(x, y, z).add(direction);

                        // Check if we can render the face
                        boolean shouldRender = !getBlockAtWorldPosition(chunkNeighbors, neighborPos.x,neighborPos.y,neighborPos.z).isSolid();
                        // Skip air blocks, already visited blocks, or faces that shouldn't render
                        if (blockType == 0 || visited[x][y] || !shouldRender) {
                            continue;
                        }

                        // Determine how far we can extend the quad
                        int width = 1;
                        while (z + width < Constants.CHUNK_SIZE &&
                                chunkData.getBlockAtPosition(x, y, z + width).getId() == blockType) {
                            width++;
                        }

                        // Extend vertically
                        int height = 1;
                        boolean extendable = true;
                        while (y + height < Constants.CHUNK_SIZE && extendable) {
                            for (int k = 0; k < width; k++) {
                                if (chunkData.getBlockAtPosition(x, y + height, z + k).getId() != blockType) {
                                    extendable = false;
                                    break;
                                }
                            }
                            if (extendable) {
                                height++;
                            }
                        }

                        // Mark the merged blocks as visited
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                visited[x][y + i] = true;
                            }
                        }

                        // Create a new quad based on the merged blocks
                        instances.add(GreedyQuad.makeInstanceDataU32(x, y, z, dir, blockType, width, height));

                        // Skip to the end of this quad
                        z += width - 1;
                    }
                }
            }
        }

        chunkMesh.setInstances(instances);
        return chunkMesh;
    }

    // Utility method to determine if a face should be rendered
    private Block getBlockAtWorldPosition(Map<Vector3i, ChunkData> chunkList, int x, int y, int z) {
        // Adjust chunk coordinates to handle negative values correctly
        int chunkX = (x < 0) ? (((x + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (x / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkY = (y < 0) ? (((y + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (y / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkZ = (z < 0) ? (((z + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (z / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;

        Vector3i key = new Vector3i(chunkX, chunkY, chunkZ);

        ChunkData localChunk = chunkList.get(key);
        if (localChunk == null) {
            return Block.AIR;
        }

        // Use adjusted coordinates to ensure positive local values within the chunk
        int localX = ((x % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localY = ((y % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localZ = ((z % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;

        return localChunk.getBlockAtPosition(localX, localY, localZ);
    }


}