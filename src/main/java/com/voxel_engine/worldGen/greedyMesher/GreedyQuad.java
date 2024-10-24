package com.voxel_engine.worldGen.greedyMesher;

import com.voxel_engine.utils.Direction;
import org.joml.Vector3i;

import java.util.*;
public class GreedyQuad {
    /*private int x, y, w, h;

    // Constructor for initialization
    public GreedyQuad(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Method to compress this quad data into the input vertices list
    public void appendVertices(
            List<Integer> vertices,
            Direction direction,
            int axis,
            int blockType
    ) {
        // Convert axis to int and get jump value
        int axisInt = axis;

        // Create vertices based on the given parameters
        int v1 = makeVertexU32(
                direction.worldToSample(axisInt, x, y),
                0,
                direction.normalIndex(),
                blockType
        );
        int v2 = makeVertexU32(
                direction.worldToSample(axisInt, x + w, y),
                0,
                direction.normalIndex(),
                blockType
        );
        int v3 = makeVertexU32(
                direction.worldToSample(axisInt, x + w, y + h),
                0,
                direction.normalIndex(),
                blockType
        );
        int v4 = makeVertexU32(
                direction.worldToSample(axisInt, x, y + h),
                0,
                direction.normalIndex(),
                blockType
        );

        // Create a list to store new vertices
        LinkedList<Integer> newVertices = new LinkedList<>(Arrays.asList(v1, v2, v3, v4));

        // Triangle vertex order may need to be reversed
        if (direction.reverseOrder()) {
            // Keep the first index but reverse the rest
            List<Integer> o = new ArrayList<>(newVertices.subList(1, newVertices.size()));
            Collections.reverse(o);
            for (int i = 1; i < newVertices.size(); i++) {
                newVertices.set(i, o.get(i - 1));
            }
        }

        if ((v1 > 0) ^ (v3 > 0)) {
            // Rotate the array to swap the triangle intersection angle
            Integer first = newVertices.pollFirst();
            newVertices.addLast(first);
        }

        // Add the new vertices to the input list
        vertices.addAll(newVertices);
    }

    // Placeholder for the makeVertexU32 method


    */
}
