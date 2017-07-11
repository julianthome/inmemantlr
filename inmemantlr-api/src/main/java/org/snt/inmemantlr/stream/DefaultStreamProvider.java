package org.snt.inmemantlr.stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;


/**
 * default stream provider used by the GenericParser
 */
public class DefaultStreamProvider implements StreamProvider {
    @Override
    public CharStream getCharStream(String s) {
        return CharStreams.fromString(s);
    }
}
