package com.voxel_engine.player;

import org.lwjgl.glfw.GLFWCursorPosCallbackI;

public class MouseHandler implements GLFWCursorPosCallbackI {
    private float lastX, lastY;
    private boolean firstMouse = true;
    private Camera camera;

    public MouseHandler(Camera camera) {
        this.camera = camera;
        this.lastX = 400; // Assume window center
        this.lastY = 300; // Assume window center
    }

    @Override
    public void invoke(long window, double xpos, double ypos) {
        if (firstMouse) {
            lastX = (float) xpos;
            lastY = (float) ypos;
            firstMouse = false;
        }

        float xOffset = (float) xpos - lastX;
        float yOffset = lastY - (float) ypos; // Invert yOffset to correct the inversion issue
        lastX = (float) xpos;
        lastY = (float) ypos;

        float sensitivity = 0.07f; // Adjust sensitivity as needed
        xOffset *= sensitivity;
        yOffset *= sensitivity;

        camera.rotate(xOffset, yOffset);
    }
}
