package worldGen;

import main.Constants;
import main.Renderer;
import org.joml.Vector3i;
import worldGen.chunk.Block;
import worldGen.chunk.ChunkData;
import worldGen.chunk.ChunkMesh;
import worldGen.chunk.ChunkRefs;
import worldGen.chunk.Block.BlockType;
import java.util.*;

public class GreedyMesher {
    private Renderer renderer;

    public GreedyMesher(Renderer renderer){
        this.renderer = renderer;
    }

    public ChunkMesh buildChunkMesh(ChunkRefs chunkRefs) {
        long[][][] axisCols = new long[3][Constants.CHUNK_SIZE_P][Constants.CHUNK_SIZE_P];
        long[][][] colFaceMasks = new long[6][Constants.CHUNK_SIZE_P][Constants.CHUNK_SIZE_P];


        /*if (chunkRefs.isAllVoxelsSame()) {
            System.out.println("All the same");
            return null;
        }*/

        // middle chunk in a 3x3
        ChunkData chunk = chunkRefs.getChunks().get(5);
        ChunkMesh mesh = new ChunkMesh(chunk);

        // creating axisCols for inner chunks
        for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    int i = (chunk.getBlocks().length == 1) ? 0 : (y * Constants.CHUNK_SIZE * Constants.CHUNK_SIZE) + (z * Constants.CHUNK_SIZE) + x;
                    addVoxelToAxisCols(chunk.getBlocks()[i], x + 1, y + 1 , z + 1, axisCols);
                }
            }
        }

        // creating axisCols for neighbor chunks
        for (int z : new int[]{0, Constants.CHUNK_SIZE_P - 1}) {
            for (int y = 0; y < Constants.CHUNK_SIZE_P; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(chunkRefs.getBlock(pos), x, y, z, axisCols);
                }
            }
        }

        for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
            for (int y : new int[]{0, Constants.CHUNK_SIZE_P - 1}) {
                for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(chunkRefs.getBlock(pos), x, y, z, axisCols);
                }
            }
        }

        for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
            for (int x : new int[]{0, Constants.CHUNK_SIZE_P - 1}) {
                for (int y = 0; y < Constants.CHUNK_SIZE_P; y++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(chunkRefs.getBlock(pos), x, y, z, axisCols);
                }
            }
        }

        // face culling
        for (int axis = 0; axis < 3; axis++) {
            for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
                for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                    long col = axisCols[axis][z][x];

                    // Descending: check if solid voxel meets air
                    colFaceMasks[2 * axis][z][x] = col & ~(col << 1);

                    // Ascending: check if air voxel meets solid
                    colFaceMasks[2 * axis + 1][z][x] = col & ~(col >> 1);
                }
            }
        }

        Map<Integer, Map<Integer, int[]>>[] data;
        data = new HashMap[6];
        for (int i = 0; i < 6; i++) {
            data[i] = new HashMap<>();
        }

        for (int axis = 0; axis < 6; axis++) {
            for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    // Skip padded by adding 1 (for x padding) and (z + 1) for (z padding)
                    long col = colFaceMasks[axis][z + 1][x + 1];

                    // Removes the rightmost padding value, because it's invalid
                    col >>= 1;
                    // Removes the leftmost padding value, because it's invalid
                    col &= ~(1L << Constants.CHUNK_SIZE);

                    while (col != 0) {
                        int y = Long.numberOfTrailingZeros(col);
                        // Clear least significant set bit
                        col &= col - 1;

                        // Get the voxel position based on axis
                        Vector3i voxelPos;
                        switch (axis) {
                            case 0: // Down
                            case 1: // Up
                                voxelPos = new Vector3i(x, y, z);
                                break;
                            case 2: // Left
                            case 3: // Right
                                voxelPos = new Vector3i(y, z, x);
                                break;
                            default: // Forward, Back
                                voxelPos = new Vector3i(x, z, y);
                        }

                        Block currentVoxel = chunkRefs.getBlockNoNeighbor(voxelPos);
                        int blockHash = currentVoxel.getBlockType().getId();
                        if(currentVoxel.getBlockType() == BlockType.AIR){
                            System.out.println("AIR");
                        }
                        Map<Integer, Map<Integer, int[]>> dataAxis = data[axis];
                        Map<Integer, int[]> dataBlockHash = dataAxis.computeIfAbsent(blockHash, k -> new HashMap<>());
                        int[] dataY = dataBlockHash.computeIfAbsent(y, k -> new int[Constants.CHUNK_SIZE_P]);

                        dataY[x] |= 1 << z;
                    }
                }
            }
        }

        List<Integer> vertices = new ArrayList<>();
        for (int axis = 0; axis < data.length; axis++) {
            Direction direction;

            switch (axis) {
                case 0: direction = Direction.DOWN; break;
                case 1: direction = Direction.UP; break;
                case 2: direction = Direction.LEFT; break;
                case 3: direction = Direction.RIGHT; break;
                case 4: direction = Direction.FORWARD; break;
                default: direction = Direction.BACK; break;
            }

            Map<Integer, Map<Integer, int[]>> blockData = data[axis];
            for (Map.Entry<Integer, Map<Integer, int[]>> entry : blockData.entrySet()) {
                int blockType = entry.getKey();
                for (Map.Entry<Integer, int[]> axisPlaneEntry : entry.getValue().entrySet()) {
                    int axisPos = axisPlaneEntry.getKey();
                    int[] plane = axisPlaneEntry.getValue();
                    List<GreedyQuad> quadsFromAxis = greedyMeshBinaryPlane(plane, Constants.CHUNK_SIZE);

                    for (GreedyQuad q : quadsFromAxis) {
                        q.appendVertices(vertices, direction, axisPos, blockType);
                    }
                }
            }
        }

        mesh.setVertices(vertices);
        if (mesh.getVerticesAsList().isEmpty()) {
            return null;
        } else {
            mesh.setIndices(generateIndices(mesh.getVerticesAsList().size()));
            return mesh;
        }
    }
    private void addVoxelToAxisCols(Block b, int x, int y, int z, long[][][] axisCols) {
        if (b.getBlockType().isSolid()) {
            // x,z - y axis
            axisCols[0][z][x] |= 1L << y;
            // z,y - x axis
            axisCols[1][y][z] |= 1L << x;
            // x,y - z axis
            axisCols[2][y][x] |= 1L << z;
        }
    }

    private static Direction getDirection(int axis) {
        switch (axis) {
            case 0:
                return Direction.DOWN;
            case 1:
                return Direction.UP;
            case 2:
                return Direction.LEFT;
            case 3:
                return Direction.RIGHT;
            case 4:
                return Direction.FORWARD;
            default:
                return Direction.BACK;
        }
    }

    public static List<Integer> generateIndices(int vertexCount) {
        int indicesCount = vertexCount / 4;
        List<Integer> indices = new ArrayList<>(indicesCount * 6);

        for (int vertIndex = 0; vertIndex < indicesCount; vertIndex++) {
            int baseIndex = vertIndex * 4;
            indices.add(baseIndex);
            indices.add(baseIndex + 1);
            indices.add(baseIndex + 2);
            indices.add(baseIndex);
            indices.add(baseIndex + 2);
            indices.add(baseIndex + 3);
        }

        return indices;
    }

    private List<GreedyQuad> greedyMeshBinaryPlane(int[] data, int lodSize) {
        List<GreedyQuad> greedyQuads = new ArrayList<>();

        for (int row = 0; row < data.length; row++) {
            int y = 0;
            while (y < lodSize) {
                y += Integer.numberOfTrailingZeros(data[row] >>> y);
                if (y >= lodSize) {
                    // reached top
                    continue;
                }
                int h = numberOfTrailingOnes(data[row] >>> y);

                // Convert height 'h' to a mask with h repeated 1s, i.e., 1 = 0b1, 2 = 0b11, 4 = 0b1111
                int hAsMask = (h >= 32) ? ~0 : (1 << h) - 1;

                int mask = hAsMask << y;

                // Grow horizontally
                int w = 1;
                while (row + w < lodSize) {
                    // Fetch bits spanning height in the next row
                    int nextRowH = (data[row + w] >>> y) & hAsMask;
                    if (nextRowH != hAsMask) {
                        break; // Can no longer expand horizontally
                    }

                    // Nuke the bits we expanded into
                    data[row + w] = data[row + w] & ~mask;

                    w++;
                }
                greedyQuads.add(new GreedyQuad(row,y,w,h)); // Create a new GreedyQuad and add it to the list

                y += h;
            }
        }
        return greedyQuads;
    }
    private static int numberOfTrailingOnes(int value) {
        return Integer.numberOfTrailingZeros(~value);
    }
}
