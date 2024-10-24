package com.voxel_engine.worldGen.chunk;

public enum Block {
    AIR(false, 0),
    DIRT(true, 1),
    GRASS(true, 2);
    private boolean isSolid;
    private int id;

    Block(boolean isSolid, int id) {
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


