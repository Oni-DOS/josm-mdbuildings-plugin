package org.openstreetmap.josm.plugins.mdbuildings;


public class BuildingsSessionStateManager {
    private static CombineNearestOneDsStrategy oneDsConfirmationSessionStrategy = null;
    private static CombineNearestOverlappingStrategy overlappingConfirmationSessionStrategy = null;

    public static CombineNearestOneDsStrategy getOneDsConfirmationSessionStrategy() {
        return oneDsConfirmationSessionStrategy;
    }

    public static void setOneDsConfirmationSessionStrategy(CombineNearestOneDsStrategy strategy) {
        oneDsConfirmationSessionStrategy = strategy;
    }

    public static CombineNearestOverlappingStrategy getOverlappingConfirmationSessionStrategy() {
        return overlappingConfirmationSessionStrategy;
    }

    public static void setOverlappingConfirmationSessionStrategy(CombineNearestOverlappingStrategy strategy) {
        overlappingConfirmationSessionStrategy = strategy;
    }
}
