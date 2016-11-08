/*
* Inmemantlr - In memory compiler for Antlr 4
*
* Copyright 2016, Julian Thomé <julian.thome@uni.lu>
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence. You may
* obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/

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

        LOGGER.debug("ast " + ast.getGrammarName());
        if ( ast.grammarType == ANTLRParser.LEXER )
            g = new InmemantlrLexerGrammar(this, ast);
        else
            g = new InmemantlrGrammar(this, ast);

        // ensure each node has pointer to surrounding grammar
        GrammarTransformPipeline.setGrammarPtr(g, ast);
        return g;
    }


}
