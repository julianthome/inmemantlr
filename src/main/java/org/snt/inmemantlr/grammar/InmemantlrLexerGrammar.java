package org.snt.inmemantlr.grammar;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.Tool;
import org.antlr.v4.tool.ANTLRToolListener;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.tool.MemoryTokenVocabParser;

import java.util.Map;

/**
 * Created by julian on 08/11/2016.
 */
public class InmemantlrLexerGrammar extends LexerGrammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(InmemantlrLexerGrammar.class);

    private String tokenVocab = "";

    public InmemantlrLexerGrammar(Tool tool, GrammarRootAST ast) {
        super(tool, ast);
    }

    public InmemantlrLexerGrammar(String grammarText) throws RecognitionException {
        super(grammarText);
    }

    public InmemantlrLexerGrammar(String grammarText, ANTLRToolListener listener) throws RecognitionException {
        super(grammarText, listener);
    }

    public InmemantlrLexerGrammar(String fileName, String grammarText, ANTLRToolListener listener) throws RecognitionException {
        super(fileName, grammarText, listener);
    }


    public void setTokenVocab(String tokenVocab) {
        LOGGER.debug("set token vocab {} {}", this.name, tokenVocab);
        this.tokenVocab = tokenVocab;
    }

    /**
     * We want to touch as little ANTR code as possible. We overload this
     * function to pretend the existence of the token vocab parser
     */
    @Override
    public void importTokensFromTokensFile() {
        LOGGER.debug("import tokens from file");
        if(!tokenVocab.isEmpty()) {
            MemoryTokenVocabParser vparser = new MemoryTokenVocabParser(this, tokenVocab);
            Map<String,Integer> tokens = vparser.load();
            LOGGER.debug("Tokens: {}", tokens);
            for (String t : tokens.keySet()) {
                if ( t.charAt(0)=='\'' ) defineStringLiteral(t, tokens.get(t));
                else defineTokenName(t, tokens.get(t));
            }
        }
    }
}
