package com.voxel_engine.render;
import java.util.List;

import com.voxel_engine.worldGen.chunk.ChunkData;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

public class ChunkMesh {
    private ChunkData chunkData;
    private Boolean initialized = false;
    private int vaoId;
    private int vboId;
    private int eboId;
    private int[] instances;
    private int[] indices = {0,1,2,0,2,3};

    public ChunkMesh(ChunkData chunkData){
        this.chunkData = chunkData;
    }

    public void init(){
        initialized = true;
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create VBO and upload vertex data
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableVertexAttribArray(0); // vert_data
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);
        glVertexAttribDivisor(0, 1);

        // Create EBO and upload index data
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Unbind the VAO
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void setInstances(List<Integer> instances){
        this.instances = instances.stream().mapToInt(Integer::intValue).toArray();
    }

    public void bindInstances(){
        // Update VBO with vertex data
        if(instances != null && instances.length > 0){
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, instances, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    public void render(Shader shader){
        if(instances == null || instances.length == 0){
            return;
        }
        glBindVertexArray(vaoId);

        shader.setUniformVector3f("worldPos", chunkData.getWorldX(), chunkData.getWorldY(), chunkData.getWorldZ());
        shader.setUniformVector3f("lightDir", 0.5f, -1.0f, 0.5f);

        glDrawElementsInstanced(GL_TRIANGLES,6, GL_UNSIGNED_INT, 0, instances.length);

        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }

    public ChunkData getChunkData(){
        return chunkData;
    }
    public Boolean getInitialized(){
        return initialized;
    }

}
