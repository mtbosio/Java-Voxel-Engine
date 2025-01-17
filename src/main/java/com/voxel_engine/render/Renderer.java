package com.voxel_engine.render;

import com.voxel_engine.worldGen.chunk.ChunkData;
import com.voxel_engine.worldGen.chunk.ChunkManager;
import org.joml.Matrix4f;
import com.voxel_engine.player.Camera;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL31C.glTexBuffer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glMultiDrawElementsIndirect;

import java.nio.IntBuffer;

public class Renderer {
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;
    private ChunkManager chunkManager;
    private final Camera camera;
    private Shader shader;
    private Matrix4f projectionMatrix;
    public Renderer(Camera camera, ChunkManager chunkManager) {
        this.camera = camera;
        this.chunkManager = chunkManager;
        init();
    }

    public void init() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.53f, 0.81f, 0.98f, 1.0f); // Light sky blue color
        // Enable face culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        // Create and compile the shader program
        shader = new Shader("main/java/com/voxel_engine/shaders/vertex_shader.glsl", "main/java/com/voxel_engine/shaders/fragment_shader.glsl");

        // Set up projection matrix
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) 800f / (float) 600f, 0.1f, 2000);
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

        // Update the view matrix based on the camera
        Matrix4f viewMatrix = camera.getViewMatrix(); // Get the view matrix from the camera

        // Pass matrices to shader
        shader.setUniformMatrix("projectionMatrix", projectionMatrix); // Set the projection matrix uniform
        shader.setUniformMatrix("viewMatrix", viewMatrix); // Set the view matrix uniform

        chunkManager.updateChunks();

        calculateFrames();

        for(ChunkMesh chunkMesh : chunkManager.getChunkMeshMap().values()){
            chunkMesh.render(shader);
        }

        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
    }


    public void cleanUp(){
        // Unbind
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        for(ChunkMesh chunkMesh : chunkManager.getChunkMeshMap().values()){
            chunkMesh.cleanup();
        }

        chunkManager.shutdown();

        shader.cleanup();
    }
    public void calculateFrames(){
        long currentTime = System.nanoTime();
        frames++;

        // Update FPS once per second
        if (currentTime - lastTime >= 1_000_000_000) {
            fps = frames;
            frames = 0;
            lastTime = currentTime;
            System.out.println("FPS: " + fps); // Print to console for now
        }
    }
}
