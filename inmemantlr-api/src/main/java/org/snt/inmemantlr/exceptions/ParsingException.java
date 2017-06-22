package org.snt.inmemantlr.exceptions;

/**
 * this exception is thrown when a parsing error occurrs
 */
public class ParsingException extends Exception {

    private static final long serialVersionUID = -8475142705514927769L;

    /**
     * constructor
     *
     * @param msg exception message
     */
    public ParsingException(String msg) {
        super(msg);
    }

    /**
     * constructor
     *
     * @param msg   exception message
     * @param cause the cause
     */
    public ParsingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}