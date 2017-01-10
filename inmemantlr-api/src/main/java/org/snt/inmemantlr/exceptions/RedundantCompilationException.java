package org.snt.inmemantlr.exceptions;


/**
 * this exception is thrown if the objects to be compiled are already compiled
 */
public class RedundantCompilationException extends CompilationException {
    public RedundantCompilationException(String msg) {
        super(msg);
    }

    public RedundantCompilationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
