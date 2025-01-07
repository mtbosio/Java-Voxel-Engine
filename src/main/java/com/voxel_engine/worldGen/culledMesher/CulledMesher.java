package com.voxel_engine.worldGen.culledMesher;

import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.utils.Direction;
import com.voxel_engine.worldGen.chunk.Block;
import com.voxel_engine.worldGen.chunk.ChunkData;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CulledMesher {
    public ChunkMesh buildChunkMesh(ChunkData chunkData, Map<Vector3i, ChunkData> chunkNeighbors) {
        ChunkMesh chunkMesh = new ChunkMesh(chunkData);

        if(chunkData.getWorldY() > 255){
            return chunkMesh;
        }

        List<Integer> instanceList = new ArrayList<>();
        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                    Block block = chunkData.getBlockAtPosition(x,y,z);
                    // [back, forward, down, up, left, right]
                    List<Block> blockNeighbors = getBlockNeighbors(chunkNeighbors, chunkData.getWorldX() + x, chunkData.getWorldY() + y, chunkData.getWorldZ() + z);
                    if(block.isSolid()){
                        if(!blockNeighbors.get(0).isSolid()){ // back is not solid
                            pushFace(instanceList, Direction.BACK, block.getId(), x,y,z); // add back face
                        }
                        if(!blockNeighbors.get(1).isSolid()){ // forward is not solid
                            pushFace(instanceList, Direction.FORWARD, block.getId(), x,y,z); // add forward face
                        }
                        if(!blockNeighbors.get(2).isSolid()){ // down is not solid
                            pushFace(instanceList,Direction.DOWN, block.getId(), x,y,z); // add down face
                        }
                        if(!blockNeighbors.get(3).isSolid()){ // up is not solid
                            pushFace(instanceList,Direction.UP, block.getId(), x,y,z); // add up face
                        }
                        if(!blockNeighbors.get(4).isSolid()){ // left is not solid
                            pushFace(instanceList,Direction.LEFT, block.getId(), x,y,z); // add left face
                        }
                        if(!blockNeighbors.get(5).isSolid()){ // right is not solid
                            pushFace(instanceList,Direction.RIGHT, block.getId(), x,y,z); // add right face
                        }
                    }
                }
            }
        }
        chunkMesh.setInstances(instanceList);
        return chunkMesh;
    }

    private void pushFace(List<Integer> instanceList, Direction direction, int blockId, int x, int y, int z){
        instanceList.add(makeInstanceDataU32(x,y,z,direction.normalIndex(),blockId, 1,1));
    }
    private static int makeInstanceDataU32(int x, int y, int z, int normal, int blockId, int width, int height) {
        // Ensure the input values fit within their respective bit limits
        x = x & 0xF;         // 4 bits
        y = y & 0xF;         // 4 bits
        z = z & 0xF;         // 4 bits
        normal = normal & 0x7;   // 3 bits
        blockId = blockId & 0x1FF; // 9 bits
        width = width & 0xF;    // 4 bits
        height = height & 0xF;  // 4 bits

        // Pack the values into a single 32-bit integer
        int packedData = (x << 28) | (y << 24) | (z << 20) | (normal << 17) |
                (blockId << 8) | (width << 4) | height;

        return packedData;
    }

    private Block getBlockAtWorldPosition(Map<Vector3i, ChunkData> chunkList, int x, int y, int z) {
        // Adjust chunk coordinates to handle negative values correctly
        int chunkX = (x < 0) ? (((x + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (x / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkY = (y < 0) ? (((y + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (y / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int chunkZ = (z < 0) ? (((z + 1) / Constants.CHUNK_SIZE) - 1) * Constants.CHUNK_SIZE : (z / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;

        Vector3i key = new Vector3i(chunkX, chunkY, chunkZ);

        ChunkData localChunk = chunkList.get(key);
        if (localChunk == null) {
            return Block.DIRT;
        }

        // Use adjusted coordinates to ensure positive local values within the chunk
        int localX = ((x % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localY = ((y % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;
        int localZ = ((z % Constants.CHUNK_SIZE) + Constants.CHUNK_SIZE) % Constants.CHUNK_SIZE;

        return localChunk.getBlockAtPosition(localX, localY, localZ);
    }


    // get neighbors [back, forward, down, up, left, right]
    private List<Block> getBlockNeighbors(Map<Vector3i, ChunkData> chunkList, int x, int y, int z){
        List<Block> result = new ArrayList<>();
        result.add(getBlockAtWorldPosition(chunkList, x, y, z-1)); // Back
        result.add(getBlockAtWorldPosition(chunkList, x, y, z+1));  // Forward
        result.add(getBlockAtWorldPosition(chunkList, x, y-1, z)); // Down
        result.add(getBlockAtWorldPosition(chunkList, x, y+1, z));  // Up
        result.add(getBlockAtWorldPosition(chunkList, x-1, y, z)); // Left
        result.add(getBlockAtWorldPosition(chunkList, x+1, y, z));  // Right
        return result;
    }

}
