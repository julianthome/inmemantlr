package org.snt.inmemantlr.exceptions;

/**
 * this exception is something is going wrong while processing an AST
 */
public class AstProcessorException extends Exception {
    private static final long serialVersionUID = -3663250114314629370L;

    /**
     * constructor
     *
     * @param msg exception message
     */
    public AstProcessorException(String msg) {
        super(msg);
    }

    /**
     * constructor
     *
     * @param msg   exception message
     * @param cause the cause
     */
    public AstProcessorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
