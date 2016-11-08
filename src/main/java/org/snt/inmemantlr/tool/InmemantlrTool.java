package org.snt.inmemantlr.tool;

import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.tool.BuildDependencyGenerator;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarTransformPipeline;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.snt.inmemantlr.grammar.InmemantlrLexerGrammar;

import java.util.List;

public class InmemantlrTool extends org.antlr.v4.Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(InmemantlrTool.class);

    @Override
    public void processGrammarsOnCommandLine() {
        List<GrammarRootAST> sortedGrammars = sortGrammarByTokenVocab(grammarFiles);

        for (GrammarRootAST t : sortedGrammars) {
            final Grammar g = createGrammar(t);
            g.fileName = t.fileName;
            if ( gen_dependencies ) {
                BuildDependencyGenerator dep =
                        new BuildDependencyGenerator(this, g);
                System.out.println(dep.getDependencies().render());

            }
            else if (errMgr.getNumErrors() == 0) {
                this.process(g);
            }
        }
    }


    public void process(Grammar g) {
        LOGGER.debug("process {}", g.name);
        this.process(g, false);
    }


    @Override
    public Grammar createGrammar(GrammarRootAST ast) {
        final Grammar g;
        if ( ast.grammarType==ANTLRParser.LEXER ) g = new InmemantlrLexerGrammar(this, ast);
        else g = new InmemantlrGrammar(this, ast);

        // ensure each node has pointer to surrounding grammar
        GrammarTransformPipeline.setGrammarPtr(g, ast);
        return g;
    }


}
