package com.example.lims.enums;

public enum UserStatus {
    ACTIVE ("Может брать книги"),
    FROZEN ("Аккаунт заморожен"),
    BLOCKED ("Аккаунт заблокирован"),
    DELITED ("Удален");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
