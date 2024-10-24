package com.voxel_engine.render;

import org.joml.Matrix4f;
import com.voxel_engine.player.Camera;
import com.voxel_engine.worldGen.chunk.ChunkManager;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glMultiDrawElementsIndirect;

import java.util.List;

public class Renderer {
    private final Camera camera;
    private Shader shader;
    private Matrix4f projectionMatrix;

    private int ssbo; // stores chunk world positions
    private int indirectBuffer; // stores the start index, and the # of following indices
    private int vbo; // stores every chunk's instance data
    private int vao;
    private int ebo;
    private int[] instances;
    private int[] indices = {0,1,2,0,2,3};
    public Renderer(Camera camera) {
        this.camera = camera;
    }

    public void init() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create and compile the shader program
        shader = new Shader("main/java/com/voxel_engine/shaders/vertex_shader.glsl", "main/java/com/voxel_engine/shaders/fragment_shader.glsl");

        // Set up projection matrix
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) 800f / (float) 600f, 0.1f, 2000);

        // Create vbo
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glEnableVertexAttribArray(0); // packed vert_data
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);
        glVertexAttribDivisor(0, 1);

        // Create EBO and upload index data
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Create SSBO
        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);

        indirectBuffer = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);

        // Unbind
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
    }

    public void checkOpenGLError() {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            switch (error) {
                case GL_INVALID_OPERATION:
                    System.err.println("GL_INVALID_OPERATION");
                    break;
                case GL_INVALID_VALUE:
                    System.err.println("GL_INVALID_VALUE");
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    System.err.println("GL_INVALID_FRAMEBUFFER_OPERATION");
                    break;
                case GL_OUT_OF_MEMORY:
                    System.err.println("GL_OUT_OF_MEMORY");
                    break;
                default:
                    System.err.println("Unknown OpenGL error: " + error);
            }
        }
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear buffers
        shader.use(); // Activate the shader program

        glBindVertexArray(vao);

        // Update the view matrix based on the camera
        Matrix4f viewMatrix = camera.getViewMatrix(); // Get the view matrix from the camera

        // Pass matrices to shader
        shader.setUniformMatrix("projectionMatrix", projectionMatrix); // Set the projection matrix uniform
        shader.setUniformMatrix("viewMatrix", viewMatrix); // Set the view matrix uniform

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, 0, 0);

        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
    }

    public void cleanUp(){
        shader.cleanup();
    }
}
