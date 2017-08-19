package ch.vorburger.minecraft.storeys.model.parser;

public class SyntaxErrorException extends Exception {

    private static final long serialVersionUID = 8664176864484967885L;

    public SyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxErrorException(String message) {
        super(message);
    }

    // TODO add stuff like line & column number, to indicate position of problem

}
