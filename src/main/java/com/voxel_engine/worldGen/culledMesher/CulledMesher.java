package com.voxel_engine.worldGen.culledMesher;

import com.voxel_engine.Driver;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.utils.Direction;
import com.voxel_engine.utils.TerrainGenerator;
import com.voxel_engine.worldGen.chunk.Block;
import com.voxel_engine.worldGen.chunk.ChunkData;

import java.util.ArrayList;
import java.util.List;

public class CulledMesher {
    private static CulledMesher instance;

    public static CulledMesher getInstance() {
        if (instance == null) {
            instance = new CulledMesher();
        }
        return instance;
    }


}
