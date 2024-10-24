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
}
