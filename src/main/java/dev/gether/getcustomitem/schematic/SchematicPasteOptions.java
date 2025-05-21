package dev.gether.getcustomitem.schematic;

public class SchematicPasteOptions {
    private boolean pasteGradually = false;
    private int blocksPerTick = 20;
    private boolean applyPhysics = false;
    private boolean trackBlocks = false;

    public static SchematicPasteOptions defaults() {
        return new SchematicPasteOptions();
    }

    public boolean isPasteGradually() {
        return pasteGradually;
    }

    public SchematicPasteOptions setPasteGradually(boolean pasteGradually) {
        this.pasteGradually = pasteGradually;
        return this;
    }

    public boolean isTrackBlocks() {
        return trackBlocks;
    }

    public SchematicPasteOptions setTrackBlocks(boolean trackBlocks) {
        this.trackBlocks = trackBlocks;
        return this;
    }

    public int getBlocksPerTick() {
        return blocksPerTick;
    }

    public SchematicPasteOptions setBlocksPerTick(int blocksPerTick) {
        this.blocksPerTick = blocksPerTick;
        return this;
    }

    public boolean isApplyPhysics() {
        return applyPhysics;
    }

    public SchematicPasteOptions setApplyPhysics(boolean applyPhysics) {
        this.applyPhysics = applyPhysics;
        return this;
    }
}