package worldGen;

import main.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ChunkMesh {
    private List<Integer> vertices = new ArrayList<Integer>();
    private List<Integer> indices = new ArrayList<Integer>();

    public List<Integer> getVertices() {
        return vertices;
    }

    public void setVertices(List<Integer> vertices) {
        this.vertices = vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }


}
