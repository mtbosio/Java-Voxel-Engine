package com.voxel_engine.utils;

import org.joml.Vector3f;
import org.joml.Vector3i;

public class Constants {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_P = CHUNK_SIZE + 2; // Padded size for binary greedy alg
    // Define offsets for the six neighboring positions in 3D space.
    public static final Vector3i[] NEIGHBOR_OFFSETS = {
            new Vector3i(16, 0, 0),  // Right
            new Vector3i(-16, 0, 0), // Left
            new Vector3i(0, 16, 0),  // Up
            new Vector3i(0, -16, 0), // Down
            new Vector3i(0, 0, 16),  // Forward
            new Vector3i(0, 0, -16)  // Backward
    };


}
