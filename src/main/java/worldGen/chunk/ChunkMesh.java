package worldGen.chunk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import main.Shader;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesh {
    private ChunkData chunkData;
    private int vaoId;
    private int vboId;
    private int eboId;
    private int[] vertices;
    private int [] indices;

    public ChunkMesh(ChunkData chunkData){
        this.chunkData = chunkData;
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create VBO and upload vertex data
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableVertexAttribArray(0); // vert_data
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);

        // Create EBO and upload index data
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        // Unbind the VAO
        glBindVertexArray(0);
    }

    public int[] getVertices() {
        return vertices;
    }
    public ArrayList<Integer> getVerticesAsList(){
        return (ArrayList<Integer>) Arrays.stream(vertices).boxed().collect(Collectors.toList());
    }

    public int[] getIndices() {
        return indices;
    }
    public ArrayList<Integer> getIndicesAsList(){
        return (ArrayList<Integer>) Arrays.stream(indices).boxed().toList();
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices.stream().mapToInt(Integer::intValue).toArray();;
    }

    public void setVertices(List<Integer> vertices){
        this.vertices = vertices.stream().mapToInt(Integer::intValue).toArray();
    }

    public void render(Shader shader){
        glBindVertexArray(vaoId);

        // Update VBO with vertex data
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Update EBO with index data
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        shader.setUniformVector2i("worldPos", chunkData.getWorldX(), chunkData.getWorldZ());
        shader.setUniformVector3f("lightDir", 0.5f, -1.0f, 0.5f);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}
