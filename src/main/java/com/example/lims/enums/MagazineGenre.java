package com.example.lims.enums;

public enum MagazineGenre {
    CHILDRENS("Детский"),
    POPULAR_SCIENTIFIC("Научно-популярный"),
    MALE("Мужской"),
    FEMALE("Женский"),
    SCIENTIFIC("Научный");

    private final String displayName;

    MagazineGenre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
