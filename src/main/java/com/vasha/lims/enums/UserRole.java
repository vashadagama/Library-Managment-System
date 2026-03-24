package main.java.com.vasha.lims.enums;

public enum UserRole {
    READER ("Читатель"),
    LIBRARIAN ("Библиотекарь"),
    ADMIN ("Администратор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
