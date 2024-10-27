package com.voxel_engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import com.voxel_engine.render.Renderer;
import com.voxel_engine.utils.TerrainGenerator;
import com.voxel_engine.worldGen.culledMesher.CulledMesher;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import com.voxel_engine.player.Camera;
import com.voxel_engine.player.MouseHandler;
import com.voxel_engine.player.PlayerInput;
import com.voxel_engine.worldGen.chunk.ChunkManager;


public class Engine {
    private long window;
    private Renderer renderer;
    private Camera camera;
    private MouseHandler mouseHandler;
    public static TerrainGenerator terrainGenerator;
    private CulledMesher culledMesher;
    private PlayerInput playerInput;
    private ChunkManager chunkManager;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Set the version of OpenGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // Required for macOS
        glfwWindowHint(GLFW_DEPTH_BITS, 24);

        // Create a window
        window = glfwCreateWindow(800, 600, "Voxel Engine", NULL, NULL);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        // Initialize OpenGL
        GL.createCapabilities();

        // create the camera and mouse handler
        camera = new Camera(new Vector3f(-5f,0f,0f), new Vector3f(1f,0f,0f), new Vector3f(0,1,0));
        playerInput = new PlayerInput(camera);
        mouseHandler = new MouseHandler(camera);

        // set up the cursor callback to handle mouse input
        glfwSetCursorPosCallback(window, new MouseHandler(camera));

        // hide the cursor and lock it to the window center for a first-person camera experience
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // create a static terrainGenerator that can be accessed from any chunk
        terrainGenerator = TerrainGenerator.getInstance();

        // create a mesher that can be accessed from the World to create meshes for chunks
        culledMesher = new CulledMesher();

        // create the chunk manager
        chunkManager = new ChunkManager(culledMesher);

        // Create the renderer
        renderer = new Renderer(camera, chunkManager);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            playerInput.handleInput(window);

            renderer.render();

            // Swap buffers and poll events
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        renderer.cleanUp();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Engine().run();
    }
}
