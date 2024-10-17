package worldGen.chunk;

import main.Shader;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import main.Renderer;
public class ChunkManager {
    private Map<Vector2i, ChunkMesh> chunkList; // List to hold all chunks

    public ChunkManager() {
        this.chunkList = new HashMap<>();
    }

    // updates all chunk lists
    private void update(){

    }

    public void addChunk(Vector2i chunkPos, ChunkMesh chunkMesh) {
        chunkList.put(chunkPos, chunkMesh);
    }

    public void removeChunk(Vector2i chunkPos) {
        chunkList.remove(chunkPos);
    }

    public void renderChunks(Shader shader) {
        for(ChunkMesh chunkMesh : chunkList.values()){
            chunkMesh.render(shader);
        }
    }
    public void cleanUp(){
        for(ChunkMesh chunkMesh : chunkList.values()){
            chunkMesh.cleanup();
        }
    }
}
