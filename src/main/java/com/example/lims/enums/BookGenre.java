package com.example.lims.enums;

public enum BookGenre {
    FICTION("Художественная литература"),
    NON_FICTION("Документальная литература"),
    SCIENCE("Наука"),
    HISTORY("История"),
    BIOGRAPHY("Биография"),
    CHILDREN("Детская литература"),
    TECHNICAL("Техническая литература"),
    ART("Искусство");

    private final String displayName;

    BookGenre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
