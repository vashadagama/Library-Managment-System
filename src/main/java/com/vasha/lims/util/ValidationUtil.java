package main.java.com.vasha.lims.util;

import java.time.LocalDate;


public class ValidationUtil {
    public static void checkNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым!");
        }
    }

    public static void checkNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null!");
        }
    }

    public static void checkNotInFuture(LocalDate date, String fieldName) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " не может быть в будущем!");
        }
    }

    public static void checkIsPositive(Integer number, String fieldName) {
        if (number == null || number <= 0) {
            throw new IllegalArgumentException("Поле " + fieldName + " должно быть положительным!");
        }
    }
}
