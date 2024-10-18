package main;

import org.joml.Vector3f;

public class Constants {
    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_SIZE_P = CHUNK_SIZE + 2; // Padded size for binary greedy alg
    public static final int CHUNK_HEIGHT = 255;
    public static final int CHUNK_HEIGHT_P = CHUNK_HEIGHT + 2; // Padded size for binary greedy alg
    public static final int CHUNK_SIZE_P2 = CHUNK_SIZE_P * CHUNK_SIZE_P;
    public static final int CHUNK_SIZE_P3 = CHUNK_SIZE_P2 * CHUNK_SIZE_P;

    // Define ADJACENT_AO_DIRS as a static list of 3D vectors, e.g.
    public static final Vector3f[] ADJACENT_AO_DIRS = {
            new Vector3f(0, -1, 0), // down
            new Vector3f(0, 1, 0),  // up
            new Vector3f(-1, 0, 0), // left
            new Vector3f(1, 0, 0),  // right
            new Vector3f(0, 0, -1), // forward
            new Vector3f(0, 0, 1)   // back
    };
}
