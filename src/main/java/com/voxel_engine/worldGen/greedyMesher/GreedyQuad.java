package com.voxel_engine.worldGen.greedyMesher;

import com.voxel_engine.utils.Direction;
import org.joml.Vector3i;

import java.util.*;
public class GreedyQuad {
    private int x, y, w, h;

    // Constructor for initialization
    public GreedyQuad(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Method to compress this quad data into the instance list
    public void appendInstance(
            List<Integer> instances,
            Direction direction,
            int axis,
            int blockType
    ) {
        Vector3i pos = direction.worldToSample(axis, x, y);
        int instance = makeInstanceDataU32(
                pos.x,
                pos.y,
                pos.z,
                direction.normalIndex(),
                blockType,
                w,
                w
        );

        instances.add(instance);

    }
    public static int makeInstanceDataU32(int x, int y, int z, int normal, int blockId, int width, int height) {
        // Ensure the input values fit within their respective bit limits
        x = x & 0xF;         // 4 bits
        y = y & 0xF;         // 4 bits
        z = z & 0xF;         // 4 bits
        normal = normal & 0x7;   // 3 bits
        blockId = blockId & 0x1FF; // 9 bits
        width = width & 0xF;    // 4 bits
        height = height & 0xF;  // 4 bits

        // Pack the values into a single 32-bit integer
        int packedData = (x << 28) | (y << 24) | (z << 20) | (normal << 17) |
                (blockId << 8) | (width << 4) | height;

        return packedData;
    }
}
