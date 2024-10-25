package com.voxel_engine.worldGen.chunk;

import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.render.Renderer;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.worldGen.culledMesher.CulledMesher;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

/**
 * ChunkManager is responsible for the handling of chunks.
 * It keeps track of chunk data.
 *
 *
 */
public class ChunkManager {
    private int WOLRD_SIZE = 3;
    private Map<Vector3i, ChunkMesh> currentlyRenderedChunks; // all chunks being rendered
    private Map<Vector3i, ChunkData> chunkDataList; // all chunks that have been initialized
    private CulledMesher culledMesher;
    private Renderer renderer;
    public ChunkManager(CulledMesher culledMesher, Renderer renderer) {
        this.currentlyRenderedChunks = new HashMap<>();
        this.chunkDataList = new HashMap<>();
        this.culledMesher = culledMesher;
        this.renderer = renderer;
        initializeWorldChunkData();
        initializeWorldChunkMesh();
    }

    private void initializeWorldChunkData(){
        for(int x = -WOLRD_SIZE; x<WOLRD_SIZE; x++){
            for(int z = -WOLRD_SIZE; z<WOLRD_SIZE; z++){
                for(int y = -WOLRD_SIZE; y<WOLRD_SIZE; y++){
                    addChunkToChunkList(new Vector3i(x,y,z), new ChunkData(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE));
                }
            }
        }
    }
    private void initializeWorldChunkMesh(){
        for(int x = -WOLRD_SIZE; x<WOLRD_SIZE; x++){
            for(int z = -WOLRD_SIZE; z<WOLRD_SIZE; z++){
                for(int y = -WOLRD_SIZE; y<WOLRD_SIZE; y++){
                    ChunkData chunkData = chunkDataList.get(new Vector3i(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE));
                    Map<Vector3i, ChunkData> neighbors = getNeighbors(chunkData);
                    ChunkMesh chunkMesh = culledMesher.buildChunkMesh(chunkData, neighbors);
                    addChunkToBeRendered(new Vector3i(x,y,z), chunkMesh);
                }
            }
        }
    }

    private Map<Vector3i, ChunkData> getNeighbors(ChunkData chunkData){
        Map<Vector3i, ChunkData> neighbors = new HashMap<>();
        Vector3i chunkPos = new Vector3i(chunkData.getWorldX(), chunkData.getWorldY(), chunkData.getWorldZ());
        // + - x
        neighbors.put(chunkPos.add(chunkPos.x + Constants.CHUNK_SIZE,0,0), chunkDataList.get(chunkPos.add(Constants.CHUNK_SIZE,0,0)));
        neighbors.put(chunkPos.add(chunkPos.x - Constants.CHUNK_SIZE,0,0), chunkDataList.get(chunkPos.sub(Constants.CHUNK_SIZE,0,0)));

        // + - y
        neighbors.put(chunkPos.add(0,chunkPos.y + Constants.CHUNK_SIZE,0), chunkDataList.get(chunkPos.add(0,Constants.CHUNK_SIZE,0)));
        neighbors.put(chunkPos.add(0,chunkPos.y - Constants.CHUNK_SIZE,0), chunkDataList.get(chunkPos.sub(0,Constants.CHUNK_SIZE,0)));

        // + - z
        neighbors.put(chunkPos.add(0,0,chunkPos.z + Constants.CHUNK_SIZE), chunkDataList.get(chunkPos.add(0,0,Constants.CHUNK_SIZE)));
        neighbors.put(chunkPos.add(0,0,chunkPos.z - Constants.CHUNK_SIZE), chunkDataList.get(chunkPos.sub(0,0,Constants.CHUNK_SIZE)));
        return neighbors;
    }
    private void addChunkToBeRendered(Vector3i chunkPos, ChunkMesh chunkMesh) {
        currentlyRenderedChunks.put(chunkPos, chunkMesh);

    }
    private void addChunkToChunkList(Vector3i chunkPos, ChunkData chunkData) {
        chunkDataList.put(chunkPos, chunkData);
    }


}
