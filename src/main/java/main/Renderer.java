package main;

import org.joml.Matrix4f;
import player.Camera;
import worldGen.chunk.ChunkManager;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import java.util.List;

public class Renderer {
    private final Camera camera;
    private Shader shader;
    private Matrix4f projectionMatrix;
    private ChunkManager chunkManager;
    public Renderer(Camera camera, ChunkManager chunkManager) {
        this.camera = camera;
        this.chunkManager = chunkManager;
    }

    public void init() {
        // Create and compile the shader program
        shader = new Shader("/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/vertex_shader.glsl", "/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/fragment_shader.glsl");

        // Set up projection matrix
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) 800f / (float) 600f, 0.1f, 100.0f);
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

        chunkManager.renderChunks(shader);

        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
    }

    public void cleanUp(){
        shader.cleanup();
    }

    private int[] listToIntArray(List<Integer> lst) {
        int[] arr = new int[lst.size()]; // Create an int array of the same size
        for (int i = 0; i < lst.size(); i++) {
            arr[i] = lst.get(i); // Fill the int array
        }
        return arr;
    }

}
