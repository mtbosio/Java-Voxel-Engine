package com.voxel_engine.render;

import com.voxel_engine.worldGen.chunk.ChunkData;
import org.joml.Matrix4f;
import com.voxel_engine.player.Camera;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
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
    private final Camera camera;
    private Shader shader;
    private Matrix4f projectionMatrix;

    private int ubo; // stores chunk world positions
    private int indirectBuffer; // stores the start index, and the # of following indices
    private int vbo; // stores every chunk's instance data
    private int vao;
    private int ebo;
    private int chunksBeingRenderedCount = 0;
    private int totalQuadsRendered = 0;
    private int[] indices = {0,1,2,0,2,3};

    public Renderer(Camera camera) {
        this.camera = camera;
        System.out.println("init");
        init();
    }

    public void init() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.53f, 0.81f, 0.98f, 1.0f); // Light sky blue color
        // Enable face culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

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

        ubo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        glBufferData(GL_UNIFORM_BUFFER, 16 * 1024, GL_DYNAMIC_DRAW); // Allocate 16KB
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo); // Bind to binding point 0

        indirectBuffer = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
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

        calculateFrames();

        // Pass matrices to shader
        shader.setUniformMatrix("projectionMatrix", projectionMatrix); // Set the projection matrix uniform
        shader.setUniformMatrix("viewMatrix", viewMatrix); // Set the view matrix uniform

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, chunksBeingRenderedCount, 0);

        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
    }

    public void addChunk(ChunkData chunkData, ChunkMesh chunkMesh){
        // indirect buffer setup
        IntBuffer buffer = MemoryUtil.memAllocInt(5);
        buffer.put(0, 6); // amount of indices to use, 6 => 3 for each triangle of a quad
        buffer.put(1, chunkMesh.getInstances().length); // amount of quads to draw
        buffer.put(2, 0); // offset into the ebo
        buffer.put(3, 0); // offset in the vertex data, these both should always be 0
        buffer.put(4, totalQuadsRendered + chunkMesh.getInstances().length); // used to reference which instance this is
        chunksBeingRenderedCount++;
        totalQuadsRendered += chunkMesh.getInstances().length;
        glBufferData(GL_DRAW_INDIRECT_BUFFER, indirectBuffer, GL_STATIC_DRAW);

        // Update UBO with chunk positions
        IntBuffer chunkPositions = MemoryUtil.memAllocInt(3);
        chunkPositions.put(0, chunkData.getWorldX());
        chunkPositions.put(1, chunkData.getWorldY());
        chunkPositions.put(2, chunkData.getWorldZ());

        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        glBufferSubData(GL_UNIFORM_BUFFER, chunksBeingRenderedCount * 12, chunkPositions); // Update UBO at the appropriate offset
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        MemoryUtil.memFree(chunkPositions);

        glBufferData(GL_ARRAY_BUFFER, chunkMesh.getInstances(), GL_STATIC_DRAW);

    }

    public void cleanUp(){
        // Unbind
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
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
