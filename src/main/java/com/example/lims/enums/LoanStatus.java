package com.example.lims.enums;

public enum LoanStatus {
    ACTIVE("Активна"),
    RETURNED("Возвращена"),
    RENEWED("Продлена"),
    OVERDUE("Просрочена"),
    LOST("Утеряна");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}