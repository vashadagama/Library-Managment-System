package com.example.lims.dto;

import com.example.lims.enums.ItemStatus;
import jakarta.validation.constraints.NotNull;

public class ItemCopyStatusUpdateDto {

    @NotNull(message = "Статус обязателен")
    private ItemStatus status;

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}