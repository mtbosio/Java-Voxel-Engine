package main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import worldGen.GreedyMesher;
import worldGen.World;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import player.Camera;
import player.MouseHandler;
import player.PlayerInput;


public class Driver {
    private long window;
    private Renderer renderer;
    private Camera camera;
    private MouseHandler mouseHandler;
    private World world;
    public static TerrainGenerator terrainGenerator;
    public static GreedyMesher greedyMesher;
    private PlayerInput playerInput;

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
        glfwWindowHint(GLFW_DEPTH_BITS, 24); // Make sure you request a depth buffer

        // Create a window
        window = glfwCreateWindow(800, 600, "Voxel Engine", NULL, NULL);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        // Initialize OpenGL
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.53f, 0.81f, 0.98f, 1.0f); // Light sky blue color


        // create the camera and mouse handler
        camera = new Camera(new Vector3f(0f,0f,5f), new Vector3f(0f,0f,-1f), new Vector3f(0,1,0));
        playerInput = new PlayerInput(camera);
        mouseHandler = new MouseHandler(camera);

        // set up the cursor callback to handle mouse input
        glfwSetCursorPosCallback(window, new MouseHandler(camera));

        // hide the cursor and lock it to the window center for a first-person camera experience
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // create a static terrainGenerator that can be accessed from any chunk
        terrainGenerator = new TerrainGenerator();

        // create a static greedyMesher that can be accessed from the World to create meshes for chunks
        greedyMesher = new GreedyMesher(renderer);

        // Create the renderer
        renderer = new Renderer(camera);
        renderer.init();

        world = new World(renderer);
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
        renderer.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Driver().run();
    }
}
