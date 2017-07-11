package org.snt.inmemantlr.stream;

import org.antlr.v4.runtime.CharStream;

/**
 * stream provider interface used for lexing
 */
public interface StreamProvider {
    CharStream getCharStream(String s);
}
