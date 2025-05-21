package dev.gether.getcustomitem.file.config;

import dev.gether.getcustomitem.region.CustomRegion;
import dev.gether.getutils.GetConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class RegionsConfig extends GetConfig {

    private Set<CustomRegion> regions = new HashSet<>();
}
