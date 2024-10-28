package com.voxel_engine.player;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f direction;
    private Vector3f up;

    private float yaw = 0f;  // Initialize facing forward
    private float pitch = 0.0f;
    private float speed = 0.1f;  // Adjust the speed to your preference

    public Camera(Vector3f position, Vector3f direction, Vector3f up) {
        this.position = position;
        this.direction = direction.normalize();
        this.up = up;
    }

    public Matrix4f getViewMatrix() {
        Vector3f center = new Vector3f(position).add(direction);
        return new Matrix4f().lookAt(position, center, up);
    }

    public void moveForward() {
        position.add(new Vector3f(direction).mul(speed));
    }

    public void moveBackward() {
        position.sub(new Vector3f(direction).mul(speed));
    }

    public void moveLeft() {
        Vector3f left = new Vector3f(direction).cross(up).normalize();
        position.sub(new Vector3f(left).mul(speed));
    }

    public void moveRight() {
        Vector3f right = new Vector3f(direction).cross(up).normalize();
        position.add(new Vector3f(right).mul(speed));
    }

    public void moveUp() {
        position.add(new Vector3f(up).mul(speed));
    }

    public void moveDown() {
        position.sub(new Vector3f(up).mul(speed));
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // Adjust yaw and pitch
    public void rotate(float yawOffset, float pitchOffset) {
        yaw += yawOffset;
        pitch += pitchOffset;

        // Limit pitch to avoid flipping (clamp between -89 and 89 degrees)
        pitch = Math.max(-89.0f, Math.min(89.0f, pitch));

        updateDirection();
    }

    // Recalculate the direction vector based on yaw and pitch
    private void updateDirection() {
        float x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float y = (float) Math.sin(Math.toRadians(pitch));
        float z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.set(x, y, z).normalize();
    }
}
