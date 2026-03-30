package main.java.com.vasha.lims.model;

import main.java.com.vasha.lims.enums.ItemStatus;

import java.util.UUID;

public class ItemCopy {
    private UUID id;
    private LibraryItem item;
    private String inventoryNumber;
    private ItemStatus status;

    public ItemCopy(LibraryItem item, String inventoryNumber) {
        this.item = item;
        this.inventoryNumber = inventoryNumber;
        this.id = UUID.randomUUID();
        this.status = ItemStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public LibraryItem getItem() {
        return item;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }
    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }


//    В классе Loan:
//    Удали поле private LibraryItem item;.
//    Вместо него добавь private ItemCopy copy;.
//    Соответственно поменяй конструктор, чтобы он принимал User user, ItemCopy copy.
//    Обнови геттеры и сеттеры (getCopy()).
//    Логика статусов в Loan:
//    В конструкторе Loan удали работу со счетчиками (нет больше item.decrementAvailableCopies()). Вместо этого меняй статус самого экземпляра: copy.setStatus(ItemStatus.LOANED);.
//    В методе returnItem() в классе Loan делай то же самое при возврате: copy.setStatus(ItemStatus.AVAILABLE);.
//    В LoanRepository:
//    Везде, где ты искал совпадения по item, теперь нужно достучаться до item через copy. Например: loan.getCopy().getItem().equals(item).
//
//    Методы equals и hashCode: В репозитории ты используешь !copies.contains(copy).
//    Чтобы метод contains работал корректно и понимал, что это один и тот же объект,
//    нужно переопределить эти методы (сравнивать по id), как ты это делал в User и LibraryItem.

    @Override
    public String toString() {
        return "ItemCopy " + "{" +
                "id=" + id +
                ", item=" + item +
                ", inventoryNumber='" + inventoryNumber + '\'' +
                ", status=" + status +
                '}';
    }
}
