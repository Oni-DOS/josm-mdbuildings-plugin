package org.openstreetmap.josm.plugins.mdbuildings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildingsTags {

    public static final Set<String> LIVING_BUILDINGS = Set.of(
        "house", "apartments", "detached", "semidetached_house", "terrace", "residential"
    );

    public static final Set<String> HOUSE_DETAILS = Set.of("detached", "semidetached_house", "terrace");

    /*
     Common building values that should not trigger the uncommon tags check.
     */
    public static final Set<String> DEFAULT_COMMON_BUILDING_VALUES = Collections.unmodifiableSet(
        Stream.concat(
            Set.of(
                "bungalow",
                "cabin",
                "commercial",
                "farmhouse",
                "garage",
                "hangar",
                "house",
                "industrial",
                "outbuilding",
                "retail",
                "service",
                "warehouse",
                "shed",
                "yes",

                "construction"
            ).stream(),
            LIVING_BUILDINGS.stream()
        ).collect(Collectors.toSet())
    );

    // Some imported data can contain other tags than building, it should be checked by mapper
    public static final List<String> UNCOMMON_NO_BUILDING_TAGS = Arrays.asList(
        "amenity",
        "leisure",
        "historic",
        "tourism"
    );

    /**
     * https://wiki.openstreetmap.org/wiki/Lifecycle_prefix
     */
    public static final Set<String> COMMON_LIFECYCLE_PREFIXES = Set.of(
        "proposed", "planned", "construction", "abandoned", "ruins", "demolished", "destroyed", "removed", "was"
    );

}
