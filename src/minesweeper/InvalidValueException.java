package minesweeper;

public class InvalidValueException extends Exception {
    String message;

    public String getErrorMessage() {
        return message;
    }

    public InvalidValueException(String s) {
        message = s;
    }
}
