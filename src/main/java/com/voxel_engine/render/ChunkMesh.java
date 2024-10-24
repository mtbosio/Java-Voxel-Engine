package com.voxel_engine.render;
import java.util.ArrayList;
import java.util.List;

import com.voxel_engine.Driver;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.utils.Direction;
import com.voxel_engine.worldGen.chunk.Block;
import com.voxel_engine.worldGen.chunk.ChunkData;

public class ChunkMesh {
    private ChunkData chunkData;
    private List<Integer> instances;

    public ChunkMesh(ChunkData chunkData){
        this.chunkData = chunkData;
        buildChunkMesh();
    }

    private void setInstances(List<Integer> instances){
        this.instances = instances;
    }
    public List<Integer> getInstances(){
        return instances;
    }

    private void buildChunkMesh() {
        List<Integer> instanceList = new ArrayList<>();
        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                    Block block = chunkData.getBlockAtPosition(x,y,z);
                    // [back, forward, down, up, left, right]
                    List<Block> blockNeighbors = Driver.chunkManager.getBlockNeighbors(chunkData.getWorldX() + x, chunkData.getWorldY() + y, chunkData.getWorldZ() + z);
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
        setInstances(instanceList);
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

}
