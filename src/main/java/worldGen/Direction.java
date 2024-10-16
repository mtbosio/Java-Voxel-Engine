package worldGen;

import org.joml.Vector3i;

public enum Direction {
    UP,
    LEFT,
    RIGHT,
    DOWN,
    FORWARD,
    BACK;

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

    public int normalIndex() {
        switch (this) {
            case LEFT:
                return 0;
            case RIGHT:
                return 1;
            case DOWN:
                return 2;
            case UP:
                return 3;
            case FORWARD:
                return 4;
            case BACK:
                return 5;
            default:
                throw new IllegalArgumentException("Unexpected direction: " + this);
        }
    }

    public boolean reverseOrder() {
        switch (this) {
            case UP:
                return true; // +1
            case DOWN:
                return false; // -1
            case LEFT:
                return false; // -1
            case RIGHT:
                return true; // +1
            case FORWARD:
                return true; // -1
            case BACK:
                return false; // +1
            default:
                return false; // Default case to handle any other values
        }
    }

    }
