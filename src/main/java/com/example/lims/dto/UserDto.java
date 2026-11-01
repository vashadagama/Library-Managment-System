package com.example.lims.dto;

import java.util.UUID;

public class UserDto {

    private UUID id;
    private String fullName;
    private String email;
    private String roleName;
    private String statusName;
    private String libraryCardNumber;
    private Integer maxActiveLoans;
    private int activeLoansCount;

    public UserDto() {
    }

    public UserDto(UUID id, String fullName, String email, String roleName,
                   String statusName, String libraryCardNumber, Integer maxActiveLoans,
                   int activeLoansCount) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roleName = roleName;
        this.statusName = statusName;
        this.libraryCardNumber = libraryCardNumber;
        this.maxActiveLoans = maxActiveLoans;
        this.activeLoansCount = activeLoansCount;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getLibraryCardNumber() { return libraryCardNumber; }
    public void setLibraryCardNumber(String libraryCardNumber) { this.libraryCardNumber = libraryCardNumber; }
    public Integer getMaxActiveLoans() { return maxActiveLoans; }
    public void setMaxActiveLoans(Integer maxActiveLoans) { this.maxActiveLoans = maxActiveLoans; }
    public int getActiveLoansCount() { return activeLoansCount; }
    public void setActiveLoansCount(int activeLoansCount) { this.activeLoansCount = activeLoansCount; }
}
