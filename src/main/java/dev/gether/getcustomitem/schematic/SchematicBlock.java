package dev.gether.getcustomitem.schematic;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SchematicBlock implements Serializable {

    private String blockData;
    private int vectorX;
    private int vectorY;
    private int vectorZ;

    public SchematicBlock(String blockData, int vectorX, int vectorY, int vectorZ) {
        this.blockData = blockData;
        this.vectorX = vectorX;
        this.vectorY = vectorY;
        this.vectorZ = vectorZ;
    }
}
