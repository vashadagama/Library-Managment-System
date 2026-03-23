package main.java.com.vasha.lims.enums;

public enum ItemStatus {
    AVAILABLE("Доступно"),
    BORROVED("Выдано"),
    UNDER_REPAIR("На ремонте"),
    LOST("Утеряно"),
    WITHDRAWN("Списано");

    private final String displayName;

    ItemStatus (String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}