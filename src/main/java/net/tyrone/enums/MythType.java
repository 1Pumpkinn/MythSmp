package net.tyrone.enums;

public enum MythType {
    WIND("Wind Myth"),
    LIGHTNING("Lightning Myth"),
    FIRE("Fire Myth"),
    EARTH("Earth Myth"),
    RICHES("Myth of Riches");

    private final String displayName;

    MythType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}