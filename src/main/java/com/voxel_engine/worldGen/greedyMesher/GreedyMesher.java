package com.voxel_engine.worldGen.greedyMesher;

import com.voxel_engine.utils.Constants;
import com.voxel_engine.render.Renderer;
import com.voxel_engine.utils.Direction;
import org.joml.Vector3i;
import com.voxel_engine.worldGen.chunk.Block;
import com.voxel_engine.worldGen.chunk.ChunkData;
import com.voxel_engine.render.ChunkMesh;
import com.voxel_engine.worldGen.chunk.ChunkRefs;
import com.voxel_engine.worldGen.chunk.Block.BlockType;
import java.util.*;

public class GreedyMesher {
    private Renderer renderer;

    public GreedyMesher(Renderer renderer){
        this.renderer = renderer;
    }

    public ChunkMesh buildChunkMesh(ChunkRefs chunkRefs) {
        // Axis 0: Tracking X-Z plane along the Y-axis, needs 4 long values per (x, z) pair to cover 256 bits
        long[][][] axisCols = new long[Constants.CHUNK_SIZE_P][Constants.CHUNK_SIZE_P][4];
        // Axis 1: Tracking Z-Y plane along the X-axis, needs 1 long value per (y, z) pair
        long[][] axisCols1 = new long[Constants.CHUNK_HEIGHT_P][Constants.CHUNK_SIZE_P];
        // Axis 2: Tracking X-Y plane along the Z-axis, needs 1 long value per (x, y) pair
        long[][] axisCols2 = new long[Constants.CHUNK_HEIGHT_P][Constants.CHUNK_SIZE_P];

        // Adjust colFaceMasks to handle the full Y-axis range of 256
        long[][][][] colFaceMasks = new long[6][Constants.CHUNK_SIZE_P][Constants.CHUNK_SIZE_P][Constants.CHUNK_HEIGHT_P];

        /*if (chunkRefs.isAllVoxelsSame()) {
            System.out.println("All the same");
            return null;
        }*/

        // middle chunk in a 3x3
        ChunkData chunk = chunkRefs.getChunks().get(5);
        ChunkMesh mesh = new ChunkMesh(chunk);

        // creating axisCols for inner chunks
        for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
            for (int y = 0; y < Constants.CHUNK_HEIGHT; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
                    int i = (chunk.getBlocks().length == 1) ? 0 : (y * Constants.CHUNK_SIZE * Constants.CHUNK_SIZE) + (z * Constants.CHUNK_SIZE) + x;
                    addVoxelToAxisCols(axisCols, axisCols1, axisCols2, chunk.getBlocks()[i], x + 1, y + 1 , z + 1);
                }
            }
        }

        // creating axisCols for neighbor chunks
        for (int z : new int[]{0, Constants.CHUNK_SIZE_P - 1}) {
            for (int y = 0; y < Constants.CHUNK_HEIGHT_P - 1; y++) {
                for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(axisCols, axisCols1, axisCols2, chunkRefs.getBlock(pos), x, y, z);
                }
            }
        }

        for (int y : new int[]{0, Constants.CHUNK_HEIGHT_P - 2}) {
            for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
                for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(axisCols, axisCols1, axisCols2, chunkRefs.getBlock(pos), x, y, z);
                }
            }
        }

        for (int x : new int[]{0, Constants.CHUNK_SIZE_P - 1}) {
            for (int y = 0; y < Constants.CHUNK_HEIGHT_P - 1; y++) {
                for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
                    Vector3i pos = new Vector3i(x, y, z).sub(1, 1, 1);
                    addVoxelToAxisCols(axisCols, axisCols1, axisCols2, chunkRefs.getBlock(pos), x, y, z);
                }
            }
        }

        // Face culling
        for (int axis = 0; axis < 3; axis++) {
            if (axis == 0) {
                // Axis 0: X-Z plane along the Y-axis
                for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
                    for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                        for (int i = 0; i < 4; i++) {
                            long col = axisCols[x][z][i];

                            // Descending: check if solid voxel meets air
                            colFaceMasks[2 * axis][x][z][i] = col & ~(col << 1);

                            // Ascending: check if air voxel meets solid
                            colFaceMasks[2 * axis + 1][x][z][i] = col & ~(col >> 1);
                        }
                    }
                }
            } else if (axis == 1) {
                // Axis 1: Z-Y plane along the X-axis
                for (int y = 0; y < Constants.CHUNK_HEIGHT_P; y++) {
                    for (int z = 0; z < Constants.CHUNK_SIZE_P; z++) {
                        int i = y / 64;
                        int bit = y % 64;

                        long col = axisCols1[y][z];

                        // Descending: check if solid voxel meets air
                        colFaceMasks[2 * axis][z][i][bit] = col & ~(col << 1);

                        // Ascending: check if air voxel meets solid
                        colFaceMasks[2 * axis + 1][z][i][bit] = col & ~(col >> 1);
                    }
                }
            } else if (axis == 2) {
                // Axis 2: X-Y plane along the Z-axis
                for (int y = 0; y < Constants.CHUNK_HEIGHT_P; y++) {
                    for (int x = 0; x < Constants.CHUNK_SIZE_P; x++) {
                        int i = y / 64;
                        int bit = y % 64;

                        long col = axisCols2[y][x];

                        // Descending: check if solid voxel meets air
                        colFaceMasks[2 * axis][x][i][bit] = col & ~(col << 1);

                        // Ascending: check if air voxel meets solid
                        colFaceMasks[2 * axis + 1][x][i][bit] = col & ~(col >> 1);
                    }
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
                    // Iterate over the 4 long values representing the 256 height levels
                    for (int part = 0; part < 4; part++) {
                        long col = colFaceMasks[axis][z + 1][x + 1][part];

                        // Removes the rightmost padding value, because it's invalid
                        col >>= 1;
                        // Removes the leftmost padding value, because it's invalid
                        col &= ~(1L << 32);  // Remove leftmost bit for a 64-bit long

                        int baseY = part * 64;  // Each part represents 64 y-levels

                        while (col != 0) {
                            int yOffset = Long.numberOfTrailingZeros(col);
                            // Clear least significant set bit
                            col &= col - 1;

                            int y = baseY + yOffset;  // Calculate the actual y value

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
                            if (currentVoxel.getBlockType() == BlockType.AIR) {
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
    public void addVoxelToAxisCols(long[][][] axisCols, long[][] axisCols1, long[][] axisCols2, Block b, int x, int y, int z) {
        if(b.getBlockType().isSolid()) {
            // Set bit for Y-Axis tracking X-Z plane
            setYAxisBit(axisCols, x, z, y);

            // Set bit for X-Axis tracking Z-Y plane
            axisCols1[y][z] |= (1L << x);

            // Set bit for Z-Axis tracking X-Y plane
            axisCols2[y][x] |= (1L << z);
        }
    }

    private void setYAxisBit(long[][][] axisCols, int x, int z, int y) {
        int index = y / 64; // Get the index of the long array (0 to 3)
        int bitPos = y % 64; // Get the bit position within the long
        axisCols[z][x][index] |= (1L << bitPos); // Set the bit
    }

    private void setFaceMask(long[][][][] colFaceMasks, int x, int z, int y, int face) {
        int index = y / 64; // Determine which long to use (0 to 3)
        int bitPos = y % 64; // Determine bit position within that long
        colFaceMasks[z][x][index][face] |= (1L << bitPos); // Set the bit
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
            int y = 0; // Initialize vertical position
            while (y < lodSize) {
                // Shift the row to the right by 'y' to find trailing zeros
                int trailingZeros = Integer.numberOfTrailingZeros(data[row] >>> y);
                // Check if no more ones can be found
                if (trailingZeros == 32) {
                    break; // No more ones in this row
                }
                // Move to the next y position based on trailing zeros
                y += trailingZeros;

                // Calculate the height of continuous ones
                int h = numberOfTrailingOnes(data[row] >>> y);

                // Ensure 'h' does not exceed the bounds of data length
                if (h + y > lodSize) {
                    h = lodSize - y; // Adjust height to fit within bounds
                }

                // Create a mask for height
                int hAsMask = (h >= 32) ? ~0 : (1 << h) - 1;
                int mask = hAsMask << y;

                // Grow horizontally
                int w = 1; // Start with width of 1
                while (row + w < data.length) { // Ensure within data bounds
                    // Fetch bits spanning height in the next row
                    int nextRowH = (data[row + w] >>> y) & hAsMask;
                    if (nextRowH != hAsMask) {
                        break; // Can no longer expand horizontally
                    }

                    // Clear bits we expanded into
                    data[row + w] = data[row + w] & ~mask;
                    w++;
                }
                // Create a new GreedyQuad and add it to the list
                greedyQuads.add(new GreedyQuad(row, y, w, h));

                // Move y position down by the height processed
                y += h;
            }
        }
        return greedyQuads;
    }

    private static int numberOfTrailingOnes(int value) {
        return Integer.numberOfTrailingZeros(~value);
    }
}
