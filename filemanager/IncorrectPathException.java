package data.filemanager;

public class IncorrectPathException extends Exception {
    public IncorrectPathException(String error) {
        super(error);
    }
}
