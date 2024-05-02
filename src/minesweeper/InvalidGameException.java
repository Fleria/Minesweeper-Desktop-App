package minesweeper;

public class InvalidGameException extends Exception {
    String message;

    public String getErrorMessage() {
        return message;
    }
    
    public InvalidGameException(String s) {
        message = s;
    }
}