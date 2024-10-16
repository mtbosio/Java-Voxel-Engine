package worldGen;

import main.Constants;
import main.Driver;
import main.Renderer;
import org.joml.Vector2i;

import java.util.HashMap;

public class World {
    private Renderer renderer;
    private HashMap<Vector2i, ChunkData> chunkList;  // Store chunks in a HashMap for easy access


    private ChunkRefs chunkRefs;

    public World(Renderer renderer) {
        this.renderer = renderer;
        this.chunkList = new HashMap<>();  // Initialize the world data map

        // Generate and store chunks in the worldData map
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Vector2i chunkPosition = new Vector2i(x * Constants.CHUNK_SIZE, z * Constants.CHUNK_SIZE);  // Assuming chunk size is 16
                ChunkData chunkData = new ChunkData(chunkPosition.x, 0, chunkPosition.y);
                chunkList.put(chunkPosition, chunkData);  // Store the chunk data
            }

        }

        // Create ChunksRefs instance with the generated chunks
        chunkRefs = ChunkRefs.tryNew(chunkList, new Vector2i(0, 0));  // Pass the center chunk position
        ChunkMesh chunkMesh = Driver.greedyMesher.buildChunkMesh(chunkRefs); // pass this to the renderer
        renderer.setMeshData(chunkMesh.getVertices(), chunkMesh.getIndices());

    }

}

