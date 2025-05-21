package dev.gether.getcustomitem.schematic;

import java.util.List;

public class Schematic {
    private String name;
    private List<SchematicBlock> blocks;

    public Schematic() {}

    public Schematic(String name, List<SchematicBlock> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    public String getName() {
        return name;
    }

    public List<SchematicBlock> getBlocks() {
        return blocks;
    }
}
