package com.voxel_engine.utils;

import org.joml.Vector3i;

public enum Direction {
    UP,
    LEFT,
    RIGHT,
    DOWN,
    FORWARD,
    BACK;

    public int normalIndex() {
        switch (this) {
            case LEFT: // -x
                return 0;
            case RIGHT: // +x
                return 1;
            case DOWN: // -y
                return 2;
            case UP: // +y
                return 3;
            case FORWARD: // +z
                return 4;
            case BACK: // -z
                return 5;
            default:
                throw new IllegalArgumentException("Unexpected direction: " + this);
        }
    }

    public Vector3i worldToSample(int axis, int x, int y) {
        switch (this) {
            case UP:
                return new Vector3i(x, axis + 1, y);
            case DOWN:
                return new Vector3i(x, axis, y);
            case LEFT:
                return new Vector3i(axis, y, x);
            case RIGHT:
                return new Vector3i(axis + 1, y, x);
            case FORWARD:
                return new Vector3i(x, y, axis);
            case BACK:
                return new Vector3i(x, y, axis + 1);
            default:
                throw new IllegalArgumentException("Unexpected direction: " + this);
        }
    }
}
