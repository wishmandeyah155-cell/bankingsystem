package securebankapp.utils;

import java.math.BigDecimal;

public class InputValidator {
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        String value = username.trim();
        return value.matches("^[A-Za-z0-9_-]{3,20}$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        String value = password.trim();
        return value.length() >= 8 && value.length() <= 128 && !value.isBlank();
    }

    public static boolean isValidAmountText(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return false;
        }
        try {
            new BigDecimal(amountText.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidMenuChoice(String input, int min, int max) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            int value = Integer.parseInt(input.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String sanitizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
