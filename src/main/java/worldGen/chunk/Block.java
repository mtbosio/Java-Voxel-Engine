package worldGen.chunk;

public class Block {
    public enum BlockType {
        AIR(false, 0),
        DIRT(true, 1),
        GRASS(true, 2);
        private boolean isSolid;
        private int id;
        private static BlockType[] blockTypes = {BlockType.AIR, BlockType.DIRT, BlockType.GRASS};

        BlockType(boolean isSolid, int id) {
            this.id = id;
            this.isSolid = isSolid;
        }

        public boolean isSolid() {
            return isSolid;
        }

        public int getId() {
            return id;
        }

    }


    private float x, y, z;
    private BlockType blockType;
    private Boolean active = false;

    public Block(BlockType blockType) {
        this.blockType = blockType;
    }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public BlockType getBlockType() { return blockType; }
}
