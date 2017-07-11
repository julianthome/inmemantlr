package org.snt.inmemantlr.stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.snt.inmemantlr.GenericParser;


/**
 * special stream provider for providing lower- and uppercase lexer
 */
public class CasedStreamProvider implements StreamProvider {

    GenericParser.CaseSensitiveType t = GenericParser.CaseSensitiveType.NONE;

    public CasedStreamProvider(GenericParser.CaseSensitiveType t) {
        this.t = t;
    }

    @Override
    public CharStream getCharStream(String s) {

        if(t == GenericParser.CaseSensitiveType.LOWER)
            s = s.toLowerCase();
        else if (t == GenericParser.CaseSensitiveType.UPPER)
            s = s.toUpperCase();

        return CharStreams.fromString(s);
    }
}
