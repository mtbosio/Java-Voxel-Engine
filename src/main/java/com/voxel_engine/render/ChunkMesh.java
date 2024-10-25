package com.voxel_engine.render;
import java.util.List;

import com.voxel_engine.worldGen.chunk.ChunkData;

public class ChunkMesh {
    private ChunkData chunkData;
    private int[] instances;

    public ChunkMesh(ChunkData chunkData){
        this.chunkData = chunkData;
    }

    public void setInstances(List<Integer> instances){
        this.instances = instances.stream().mapToInt(Integer::intValue).toArray();
    }
    public int[] getInstances(){
        return instances;
    }



}
