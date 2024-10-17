package main;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.file.Files;
import java.nio.file.Path;

public class Shader {
    private int programId;

    public Shader(String vertexPath, String fragmentPath) {
        programId = createShaderProgram(vertexPath, fragmentPath);
    }

    private int createShaderProgram(String vertexPath, String fragmentPath) {
        int programId = GL20.glCreateProgram();
        int vertexShaderId = loadShader(vertexPath, GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = loadShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        GL20.glLinkProgram(programId);

        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);

        return programId;
    }

    private int loadShader(String path, int type) {
        int shaderId = GL20.glCreateShader(type);
        String shaderSource = readFile(path);
        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Failed to compile shader: " + GL20.glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    private String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUniformMatrix(String name, Matrix4f matrix) {
        int location = GL20.glGetUniformLocation(programId, name);
        // Convert Matrix4f to float array and upload to shader
        float[] matrixArray = new float[16];
        matrix.get(matrixArray);
        GL20.glUniformMatrix4fv(location, false, matrixArray);
    }
    public void setUniformVector2i(String name, int x, int y) {
        int location = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform2i(location, x, y);
    }
    public void setUniformVector3f(String name, float x,float y, float z) {
        int location = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform3f(location, x, y, z);
    }
    public void use() {
        GL20.glUseProgram(programId);
    }

    public void cleanup() {
        GL20.glDeleteProgram(programId);
    }


}
