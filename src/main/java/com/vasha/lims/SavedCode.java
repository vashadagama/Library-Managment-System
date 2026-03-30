package main.java.com.vasha.lims;

import main.java.com.vasha.lims.enums.ItemStatus;

public class SavedCode {
    
       public final void incrementTotalCopies() {
        this.totalCopies += 1;
        this.availableCopies += 1;
    }

    public final void decrementTotalCopies() {
        if (totalCopies > 0 && availableCopies > 0) {
            this.totalCopies -= 1;
            this.availableCopies -= 1;
        } else {
            throw new IllegalArgumentException("Невозможно списать книгу: доступных копий нет или фонд пуст.");
        }
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public final void incrementAvailableCopies() {
        if (availableCopies < totalCopies) {
            this.availableCopies += 1;
        } else {
            throw new IllegalArgumentException("Количество доступных копий не может превышать общее количество копий!");
        }
    }

    public final void decrementAvailableCopies() {
        if (availableCopies > 0) {
            this.availableCopies -= 1;
        } else {
            throw new IllegalArgumentException("Нет доступных копий для выдачи!");
        }
    }

    public ItemStatus getStatus() {
        return status;
    }

    public final void setStatus(ItemStatus status) {
        checkNotNull(status, "Статус предмета");
        this.status = status;
    }


}
