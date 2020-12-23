package aroundearth;

public class GameEndException extends Exception {
    private static final long serialVersionUID = 1L;
    public GameEndException() {}
    public GameEndException(String msg) {
        super(msg);
    }
}
