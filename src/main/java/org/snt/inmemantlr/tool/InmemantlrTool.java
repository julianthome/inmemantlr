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

import org.antlr.v4.misc.Graph;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarTransformPipeline;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTErrorNode;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.snt.inmemantlr.grammar.InmemantlrLexerGrammar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InmemantlrTool extends org.antlr.v4.Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(InmemantlrTool.class);
    private static final long serialVersionUID = 898401600890559769L;


    public void process(Grammar g) {
        LOGGER.debug("process {}", g.name);
        this.process(g, false);
    }


    @Override
    public Grammar createGrammar(GrammarRootAST ast) {
        final Grammar g;

        LOGGER.debug("ast " + ast.getGrammarName());
        if (ast.grammarType == ANTLRParser.LEXER)
            g = new InmemantlrLexerGrammar(this, ast);
        else
            g = new InmemantlrGrammar(this, ast);

        // ensure each node has pointer to surrounding grammar
        GrammarTransformPipeline.setGrammarPtr(g, ast);
        return g;
    }


    public Set<GrammarRootAST> sortGrammarByTokenVocab(Set<String> gcs) {
        Graph<String> g = new Graph<String>();
        List<GrammarRootAST> roots = new ArrayList<GrammarRootAST>();
        for (String gc : gcs) {
            GrammarAST t = parseGrammarFromString(gc);
            if (t == null || t instanceof GrammarASTErrorNode)
                continue;

            if (((GrammarRootAST) t).hasErrors)
                continue;

            GrammarRootAST root = (GrammarRootAST) t;
            roots.add(root);
            root.fileName = root.getGrammarName();
            String grammarName = root.getChild(0).getText();

            GrammarAST tokenVocabNode = findOptionValueAST(root, "tokenVocab");
            // Make grammars depend on any tokenVocab options
            if (tokenVocabNode != null) {
                String vocabName = tokenVocabNode.getText();
                g.addEdge(grammarName, vocabName);
            }
            // add cycle to graph so we always process a grammar if no error
            // even if no dependency
            g.addEdge(grammarName, grammarName);
        }

        List<String> sortedGrammarNames = g.sort();

        LinkedHashSet<GrammarRootAST> sortedRoots = new LinkedHashSet<GrammarRootAST>();
        for (String grammarName : sortedGrammarNames) {
            for (GrammarRootAST root : roots) {
                if (root.getGrammarName().equals(grammarName)) {
                    sortedRoots.add(root);
                    break;
                }
            }
        }
        return sortedRoots;
    }

}
