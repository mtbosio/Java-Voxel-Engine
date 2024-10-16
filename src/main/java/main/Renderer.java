package main;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import player.Camera;
import worldGen.GreedyQuad;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Renderer {
    private final Camera camera;
    private int vaoId;
    private int vboId;
    private int eboId;
    private Shader shader;
    private Matrix4f projectionMatrix;
    private int[] indices;
    private int[] vertices;
    public Renderer(Camera camera) {
        this.camera = camera;
    }

    public void init() {
        // Create the vertex array object (VAO)
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create VBO for vertex data
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableVertexAttribArray(0); // vert_data
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT,  Integer.BYTES, 0);

        // Create EBO for index data
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Create and compile the shader program
        shader = new Shader("/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/vertex_shader.glsl", "/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/fragment_shader.glsl");

        // Set up projection matrix
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) 800f / (float) 600f, 0.1f, 100.0f);
    }

    // Method to set mesh data
    public void setMeshData(List<Integer> vertices, List<Integer> indices) {
        this.indices = listToIntArray(indices);
        this.vertices = listToIntArray(vertices);
    }

    public void updateInstanceBuffer() {
        glBindVertexArray(vaoId);

        // Update VBO with vertex data
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Update EBO with index data
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
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
        shader.setUniform("projectionMatrix", projectionMatrix); // Set the projection matrix uniform
        shader.setUniform("viewMatrix", viewMatrix); // Set the view matrix uniform

        updateInstanceBuffer();

        // Bind the VAO and draw the elements
        glBindVertexArray(vaoId);

        // Draw the static mesh
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
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


/*package main;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import player.Camera;
import worldGen.GreedyQuad;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Renderer {
    private final Camera camera;
    private int vaoId;
    private int vboId;
    private int eboId;
    private Shader shader;
    private Matrix4f projectionMatrix;
    private int[] indices = {
            0, 1, 2, // First triangle: bottom-left, bottom-right, top-left
            2, 1, 3  // Second triangle: top-left, bottom-right, top-right
    };
    private int[] vertices = {
            -1, -1, 0, // Bottom-left vertex (-0.5, -0.5, 0)
            1, -1, 0, // Bottom-right vertex (0.5, -0.5, 0)
            -1,  1, 0, // Top-left vertex (-0.5, 0.5, 0)
            1,  1, 0  // Top-right vertex (0.5, 0.5, 0)
    };


    public Renderer(Camera camera) {
        this.camera = camera;
    }

    public void init() {
        // Create the vertex array object (VAO)
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create VBO for vertex data
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        glVertexAttribIPointer(0, 3, GL_UNSIGNED_INT, 3 * Integer.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Create and compile the shader program
        shader = new Shader("/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/test_vertex_shader.glsl", "/Users/mb08eight/Documents/JavaVoxelEngine/src/main/shaders/fragment_shader.glsl");

        // Set up projection matrix
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) 800f / (float) 600f, 0.1f, 100.0f);
    }

    // Method to set mesh data
    public void setMeshData(List<Integer> vertices, List<Integer> indices) {
        this.indices = listToIntArray(indices);
        this.vertices = listToIntArray(vertices);
        // Process each vertex
        System.out.println(indices);
        System.out.println(vertices);


    }

    public void updateInstanceBuffer() {
        glBindVertexArray(vaoId);

        // Update VBO with vertex data
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        IntBuffer vertexBuffer = BufferUtils.createIntBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip(); // Prepare buffer for reading
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Update EBO with index data
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices);
        indexBuffer.flip(); // Prepare buffer for reading
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);


        // Unbind the VAO to prevent accidental modifications
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
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
        shader.setUniform("projectionMatrix", projectionMatrix); // Set the projection matrix uniform
        shader.setUniform("viewMatrix", viewMatrix); // Set the view matrix uniform

        updateInstanceBuffer();

        // Bind the VAO and draw the elements
        glBindVertexArray(vaoId);

        // Draw the static mesh
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        checkOpenGLError();
        glBindVertexArray(0); // Unbind VAO
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
        shader.cleanup();
    }

    private int[] listToIntArray(List<Integer> lst) {
        int[] arr = new int[lst.size()]; // Create an int array of the same size
        for (int i = 0; i < lst.size(); i++) {
            arr[i] = lst.get(i); // Fill the int array
        }
        return arr;
    }

}*/