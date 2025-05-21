package dev.gether.getcustomitem.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomRegion {
    private int priority;
    private String name;
    private Location pos1;
    private Location pos2;
    private Set<String> disabledItems;
    private Set<String> allowedItems;
}