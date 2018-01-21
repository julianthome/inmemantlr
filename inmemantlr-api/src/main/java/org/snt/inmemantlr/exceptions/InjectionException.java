package org.snt.inmemantlr.exceptions;

/**
 * this exception is something is going wrong when injecting subtree into AST
 */
public class InjectionException extends Exception {
    private static final long serialVersionUID = -3663250114312529370L;

    /**
     * constructor
     *
     * @param msg exception message
     */
    public InjectionException(String msg) {
        super(msg);
    }

    /**
     * constructor
     *
     * @param msg   exception message
     * @param cause the cause
     */
    public InjectionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
