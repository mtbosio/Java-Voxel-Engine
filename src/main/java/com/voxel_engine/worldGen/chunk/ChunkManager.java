package com.voxel_engine.worldGen.chunk;

import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.utils.Direction;
import com.voxel_engine.worldGen.culledMesher.CulledMesher;
import com.voxel_engine.worldGen.greedyMesher.GreedyMesher;
import com.voxel_engine.worldGen.greedyMesher.GreedyMesher2;
import com.voxel_engine.worldGen.greedyMesher.GreedyQuad;
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
    private final static int WORLD_SIZE = 3;
    private Map<Vector3i, ChunkMesh> currentlyRenderedChunks; // all chunks being rendered
    private Map<Vector3i, ChunkData> chunkDataList; // all chunks that have been initialized
    private GreedyMesher2 greedyMesher;
    public ChunkManager(GreedyMesher2 greedyMesher) {
        this.currentlyRenderedChunks = new HashMap<>();
        this.chunkDataList = new HashMap<>();
        this.greedyMesher = greedyMesher;
        initializeWorldChunkData();
        initializeWorldChunkMesh();
        /*ChunkData chunkData = new ChunkData(0,0,0);
        ChunkMesh chunkMesh = new ChunkMesh(chunkData);
        List<Integer> instances = new ArrayList<Integer>();
        instances.add(GreedyQuad.makeInstanceDataU32(0,10,0, Direction.UP.normalIndex(), 1,10,10));
        instances.add(GreedyQuad.makeInstanceDataU32(0,0,0, Direction.DOWN.normalIndex(), 1,10,10));
        instances.add(GreedyQuad.makeInstanceDataU32(0,0,0, Direction.LEFT.normalIndex(), 1,10,10));
        instances.add(GreedyQuad.makeInstanceDataU32(10,0,0, Direction.RIGHT.normalIndex(), 1,10,10));
        instances.add(GreedyQuad.makeInstanceDataU32(0,0,10, Direction.FORWARD.normalIndex(), 1,10,10));
        instances.add(GreedyQuad.makeInstanceDataU32(0,0,0, Direction.BACK.normalIndex(), 1,10,10));

        chunkMesh.setInstances(instances);
        currentlyRenderedChunks.put(new Vector3i(0,0,0), chunkMesh);*/
    }



    private void initializeWorldChunkData(){
        for(int x = -WORLD_SIZE; x< WORLD_SIZE; x++){
            for(int z = -WORLD_SIZE; z< WORLD_SIZE; z++){
                for(int y = WORLD_SIZE; y< WORLD_SIZE + 10; y++){
                    addChunkToChunkList(new Vector3i(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE), new ChunkData(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE));
                }
            }
        }
    }
    private void initializeWorldChunkMesh(){
        for(int x = -WORLD_SIZE; x< WORLD_SIZE; x++){
            for(int z = -WORLD_SIZE; z< WORLD_SIZE; z++){
                for(int y = WORLD_SIZE; y< WORLD_SIZE + 10; y++){
                    ChunkData chunkData = chunkDataList.get(new Vector3i(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE));
                    Map<Vector3i, ChunkData> neighbors = getNeighbors(chunkData);
                    ChunkMesh chunkMesh = greedyMesher.buildChunkMesh(chunkData, neighbors);
                    addChunkToBeRendered(new Vector3i(x * Constants.CHUNK_SIZE,y * Constants.CHUNK_SIZE,z * Constants.CHUNK_SIZE), chunkMesh);
                }
            }
        }
    }

    private Map<Vector3i, ChunkData> getNeighbors(ChunkData chunkData) {
        Map<Vector3i, ChunkData> neighbors = new HashMap<>();
        Vector3i chunkPos = new Vector3i(chunkData.getWorldX(), chunkData.getWorldY(), chunkData.getWorldZ());
        neighbors.put(chunkPos, chunkData);

        // + - x
        addNeighbor(neighbors, new Vector3i(chunkPos).add(Constants.CHUNK_SIZE, 0, 0));
        addNeighbor(neighbors, new Vector3i(chunkPos).sub(Constants.CHUNK_SIZE, 0, 0));

        // + - y
        addNeighbor(neighbors, new Vector3i(chunkPos).add(0, Constants.CHUNK_SIZE, 0));
        addNeighbor(neighbors, new Vector3i(chunkPos).sub(0, Constants.CHUNK_SIZE, 0));

        // + - z
        addNeighbor(neighbors, new Vector3i(chunkPos).add(0, 0, Constants.CHUNK_SIZE));
        addNeighbor(neighbors, new Vector3i(chunkPos).sub(0, 0, Constants.CHUNK_SIZE));

        neighbors.entrySet().removeIf(entry -> entry.getValue() == null);

        return neighbors;
    }

    private void addNeighbor(Map<Vector3i, ChunkData> neighbors, Vector3i pos) {
        ChunkData neighbor = chunkDataList.get(pos);
        if (neighbor != null) {
            neighbors.put(pos, neighbor);
        }
    }


    private void addChunkToBeRendered(Vector3i chunkPos, ChunkMesh chunkMesh) {
        currentlyRenderedChunks.put(chunkPos, chunkMesh);
    }
    private void addChunkToChunkList(Vector3i chunkPos, ChunkData chunkData) {
        chunkDataList.put(chunkPos, chunkData);
    }
    public  Map<Vector3i, ChunkMesh> getCurrentlyRenderedChunks(){
        return currentlyRenderedChunks;
    }

}
