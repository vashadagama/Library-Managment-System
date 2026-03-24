package main.java.com.vasha.lims.util;

public class ValidationUtil {
    public static void checkNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()){
            throw new IllegalArgumentException(fieldName + " не может быть пустым!");
        }
    }

    public static void checkNotNull(Object value, String fieldName){
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null!");
        }
    }
}
