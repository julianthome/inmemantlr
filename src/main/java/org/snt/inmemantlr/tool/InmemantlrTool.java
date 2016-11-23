/**
 * Inmemantlr - In memory compiler for Antlr 4
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be imported in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

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
import org.snt.inmemantlr.comp.StringCodeGenPipeline;
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.snt.inmemantlr.grammar.InmemantlrLexerGrammar;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InmemantlrTool extends org.antlr.v4.Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(InmemantlrTool.class);
    private static final long serialVersionUID = 898401600890559769L;

    private Map<String, StringCodeGenPipeline> pip = new HashMap();
    private Map<String, GrammarRootAST> ast = new HashMap();

    private List<String> order = new Vector();
    private Set<String> imported = new HashSet();

    public InmemantlrTool() {
        //gen_dependencies = true;
    }

    public void process(Grammar g) {
        super.process(g, false);
    }


    @Override
    public Grammar createGrammar(GrammarRootAST ast) {
        final Grammar g;

        LOGGER.debug("ast " + ast.getGrammarName());

        if (ast.grammarType == ANTLRParser.LEXER) {
            g = new InmemantlrLexerGrammar(this, ast);
        } else {
            g = new InmemantlrGrammar(this, ast);
        }
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
                    LOGGER.debug("add to ast buffer {}", grammarName);
                    ast.put(grammarName,root);
                    order.add(grammarName);
                    sortedRoots.add(root);
                    break;
                }
            }
        }

        return sortedRoots;
    }

    @Override
    public Grammar loadImportedGrammar(Grammar g, GrammarAST nameNode) throws
            IOException {
        String name = nameNode.getText();

        LOGGER.debug("LOAD " + name);

        assert pip.containsKey(name);
        imported.add(name);

        if(pip.containsKey(name))
            return pip.get(name).getG();

        return null;
    }

    public StringCodeGenPipeline createPipeline(GrammarRootAST ast) {

        if(this.pip.containsKey(ast.getGrammarName()))
            return pip.get(ast.getGrammarName());

        LOGGER.debug("create grammar {}", ast.getGrammarName());
        final Grammar g = createGrammar(ast);
        g.fileName = g.name;
        StringCodeGenPipeline spip = new StringCodeGenPipeline(g);
        pip.put(g.name,spip);
        return spip;
    }

    public Set<StringCodeGenPipeline> getPipelines() {
        Set<StringCodeGenPipeline> ret = new LinkedHashSet<>();
        order.stream().filter( s -> pip.containsKey(s)).forEach( s -> ret.add
                (pip.get(s)));
        return ret;
    }

    public StringCodeGenPipeline getMainPipeline() {

        assert order.size() == ast.size();
        assert order.size() > 0;

        StringCodeGenPipeline scp = pip.get(order.get(order.size()-1));

        LOGGER.debug("Main pipeline {}", scp.getG().name);
        return scp;
    }

    public boolean isImported(String name) {
        return imported.contains(name);
    }

    public Set<StringCodeGenPipeline> getImported() {

        return pip.values().stream().filter(v ->
                imported
                .contains(v.getG()
                .name)).collect(Collectors.toSet());

    }



}
