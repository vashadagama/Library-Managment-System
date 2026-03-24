package main.java.com.vasha.lims.enums;

public enum UserStatus {
    ACTIVE ("Может брать книги"),
    FROZEN ("Аккаунт заморожен"),
    TERMINATED ("Удален/заблокирован");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
