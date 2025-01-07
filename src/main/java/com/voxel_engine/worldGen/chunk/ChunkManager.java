package com.voxel_engine.worldGen.chunk;
import com.voxel_engine.player.Camera;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.render.Shader;
import com.voxel_engine.utils.Constants;
import com.voxel_engine.worldGen.culledMesher.CulledMesher;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private static final int WORLD_SIZE = 5;
    private final Map<Vector3i, ChunkData> chunkDataMap;
    private final Map<Vector3i, ChunkMesh> chunkMeshMap;
    private final Map<Vector3i, Future<ChunkData>> chunkGenerationTasks; // Track chunk generation tasks
    private final ExecutorService executorService;
    private Camera player;
    private CulledMesher culledMesher;
    private final int renderDistance = 5;

    public ChunkManager(CulledMesher culledMesher, Camera player) {
        this.player = player;
        this.culledMesher = culledMesher;
        this.chunkDataMap = new ConcurrentHashMap<>();
        this.chunkMeshMap = new HashMap<>();
        this.chunkGenerationTasks = new HashMap<>();
        initializeWorldChunkData();
        int threadCount = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(threadCount); // Thread pool for chunk generation
    }

    public void initializeWorldChunkData() {
        for (int x = -WORLD_SIZE; x < WORLD_SIZE; x++) {
            for (int z = -WORLD_SIZE; z < WORLD_SIZE; z++) {
                for (int y = 4; y < 10; y++) {
                    Vector3i chunkPos = new Vector3i(x * Constants.CHUNK_SIZE, y * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);
                    chunkDataMap.put(chunkPos, new ChunkData(chunkPos.x, chunkPos.y, chunkPos.z));
                }
            }
        }
    }

    public void initializeWorldChunkMesh() {
        for (Vector3i chunkPos : chunkDataMap.keySet()) {
            ChunkData chunkData = chunkDataMap.get(chunkPos);
            Map<Vector3i, ChunkData> neighbors = getOrCalculateNeighbors(chunkData);
            ChunkMesh chunkMesh = culledMesher.buildChunkMesh(chunkData, neighbors);
            chunkMesh.init();
            chunkMesh.bindInstances();
            chunkMeshMap.put(chunkPos, chunkMesh);
        }
    }

    // Asynchronously generates or retrieves a chunk at a specified chunk position
    public ChunkData getOrCreateChunk(int chunkX, int chunkY, int chunkZ) {
        Vector3i chunkKey = new Vector3i(chunkX, chunkY, chunkZ);

        // Check if the chunk is already generated or being generated
        if (chunkDataMap.containsKey(chunkKey)) {
            return chunkDataMap.get(chunkKey);
        }

        if (!chunkGenerationTasks.containsKey(chunkKey)) {
            // Submit a chunk generation task if not already submitted
            Future<ChunkData> futureChunkData = executorService.submit(new ChunkGenerationTask(chunkX, chunkY, chunkZ));
            chunkGenerationTasks.put(chunkKey, futureChunkData);
        }

        try {
            // Retrieve generated chunk data when ready
            ChunkData chunkData = chunkGenerationTasks.get(chunkKey).get();
            chunkDataMap.put(chunkKey, chunkData);
            Map<Vector3i, ChunkData> neighbors = getOrCalculateNeighbors(chunkData);
            ChunkMesh chunkMesh = culledMesher.buildChunkMesh(chunkData, neighbors);
            if(!chunkMesh.getInitialized()){
                chunkMesh.init();
            }
            chunkMesh.bindInstances();
            chunkMeshMap.put(chunkKey, chunkMesh);
            chunkGenerationTasks.remove(chunkKey); // Remove task after completion
            return chunkData;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null; // Return null or handle error appropriately
        }
    }

    // Updates all loaded chunks, such as rebuilding the mesh when block data changes
    public void updateChunks() {
        Vector3i cameraChunkPosition = getChunkPosition();
        Set<Vector3i> chunksInRange = calculateChunksInRange(cameraChunkPosition);

        // Load chunks that are within render distance and not already loaded
        for (Vector3i chunkPos : chunksInRange) {
            if (!chunkDataMap.containsKey(chunkPos) && !chunkGenerationTasks.containsKey(chunkPos)) {
                getOrCreateChunk(chunkPos.x, chunkPos.y, chunkPos.z);
            }
        }

        // Unload chunks that are outside of render distance
        chunkDataMap.keySet().removeIf(chunkPos -> {
            if (!chunksInRange.contains(chunkPos)) {
                chunkMeshMap.remove(chunkPos); // Remove chunk mesh
                return true; // Indicate to remove from chunkDataMap
            }
            return false;
        });
    }

    private Set<Vector3i> calculateChunksInRange(Vector3i cameraChunkPosition) {
        Set<Vector3i> chunksInRange = new HashSet<>();
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    Vector3i chunkPos = new Vector3i(
                            cameraChunkPosition.x + x * Constants.CHUNK_SIZE,
                            cameraChunkPosition.y + y * Constants.CHUNK_SIZE,
                            cameraChunkPosition.z + z * Constants.CHUNK_SIZE
                    );
                    chunksInRange.add(chunkPos);
                }
            }
        }
        return chunksInRange;
    }

    // Converts world position to chunk position
    private Vector3i getChunkPosition() {
        Vector3f playerPos = player.getPosition();
        int playerChunkX = (int) Math.floor(playerPos.x / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int playerChunkY = (int) Math.floor(playerPos.y / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        int playerChunkZ = (int) Math.floor(playerPos.z / Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
        return new Vector3i(playerChunkX, playerChunkY, playerChunkZ);
    }

    private Map<Vector3i, ChunkData> getOrCalculateNeighbors(ChunkData chunkData) {
        if ((chunkData.getNeighbors()) != null) {
            if(chunkData.getNeighbors().size() == 7){
                return chunkData.getNeighbors();
            }
        }

        Map<Vector3i, ChunkData> neighbors = new ConcurrentHashMap<>();
        Vector3i chunkPos = new Vector3i(chunkData.getWorldX(), chunkData.getWorldY(), chunkData.getWorldZ());

        for (Vector3i offset : Constants.NEIGHBOR_OFFSETS) {
            Vector3i neighborPos = new Vector3i(chunkPos).add(offset);
            ChunkData neighbor = chunkDataMap.get(neighborPos);
            if (neighbor != null) {
                neighbors.put(neighborPos, neighbor);
            } else {
                Future<ChunkData> futureChunkData = chunkGenerationTasks.computeIfAbsent(neighborPos, pos ->
                        executorService.submit(new ChunkGenerationTask(pos.x, pos.y, pos.z))
                );

                try {
                    if (futureChunkData.isDone()) {
                        ChunkData cd = chunkGenerationTasks.get(neighborPos).get();
                        chunkDataMap.put(neighborPos, cd); // Ensure it's added to the map
                        neighbors.put(neighborPos, cd);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // Handle or log error; consider a fallback to prevent disruptions
                }
            }
        }
        neighbors.put(chunkPos, chunkData);
        chunkData.setNeighbors(neighbors);
        return neighbors;
    }

    // Renders all chunk meshes
    public Map<Vector3i, ChunkMesh> getChunkMeshMap(){
        return chunkMeshMap;
    }

    // Cleanup method to shut down the executor service
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Task for generating chunk data asynchronously
    private class ChunkGenerationTask implements Callable<ChunkData> {
        private final int chunkX;
        private final int chunkY;
        private final int chunkZ;

        public ChunkGenerationTask(int chunkX, int chunkY, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            this.chunkZ = chunkZ;
        }

        @Override
        public ChunkData call() {
            // Simulate complex chunk data generation
            ChunkData chunkData = new ChunkData(chunkX, chunkY, chunkZ);
            // Initialize chunk blocks or perform heavy computations as needed
            // For example, setting blocks based on terrain noise
            return chunkData;
        }
    }
}
