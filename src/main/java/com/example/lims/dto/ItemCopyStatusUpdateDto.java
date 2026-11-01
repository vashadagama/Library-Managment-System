package com.example.lims.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemCopyStatusUpdateDto {

    @NotBlank(message = "Статус обязателен")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
