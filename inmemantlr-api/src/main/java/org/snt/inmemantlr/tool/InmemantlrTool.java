/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
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
import org.antlr.v4.tool.*;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTErrorNode;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.comp.StringCodeGenPipeline;
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.snt.inmemantlr.grammar.InmemantlrLexerGrammar;
import org.snt.inmemantlr.utils.Tuple;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toCollection;

/**
 * special tool class that implements in-memory operations for
 * the original antlr tool
 */
public class InmemantlrTool extends org.antlr.v4.Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(InmemantlrTool.class);

    private Map<String, StringCodeGenPipeline> pip = new HashMap<>();
    private Map<String, GrammarRootAST> ast = new HashMap<>();

    private Map<String, String> tokvok = new HashMap<>();

    private List<String> order = new Vector<>();
    private Set<String> imported = new HashSet<>();

    private String parserName = "";
    private String lexerName = "";

    public InmemantlrTool() {
        gen_dependencies = true;
    }

    /**
     * process a grammar
     *
     * @param g grammar
     */
    public void process(Grammar g) {
        process(g, false);
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

    /**
     * wrapper for sorting grammars based on the imported token vocab
     *
     * @param gcs grammar content collection
     * @return set of grammar asts
     */
    public Set<GrammarRootAST> sortGrammarByTokenVocab(Set<String> gcs) {
        Graph<String> g = new Graph<>();
        List<GrammarRootAST> roots = new ArrayList<>();
        for (String gc : gcs) {
            GrammarAST t = parseGrammarFromString(gc);
            if (t instanceof GrammarASTErrorNode)
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

        LinkedHashSet<GrammarRootAST> sortedRoots = new LinkedHashSet<>();
        for (String grammarName : sortedGrammarNames) {
            for (GrammarRootAST root : roots) {
                if (root.getGrammarName().equals(grammarName)) {
                    LOGGER.debug("add to ast buffer {}", grammarName);
                    ast.put(grammarName, root);
                    order.add(grammarName);
                    sortedRoots.add(root);
                    break;
                }
            }
        }

        return sortedRoots;
    }

    @Override
    public Grammar loadImportedGrammar(Grammar g, GrammarAST nameNode) throws IOException {
        String name = nameNode.getText();

        imported.add(name);

        if (pip.containsKey(name))
            return pip.get(name).getG();

        return null;
    }

    /**
     * return package prefix if configured by user
     *
     * @return package prefix
     */
    public String getPackagePrefix() {
        return genPackage != null && genPackage.length() > 0 ? genPackage + "" +
                "." :
                "";
    }

    /**
     * create code generation pipeline from grammar ast
     *
     * @param ast grammar ast
     * @return string code generation pipeline
     */
    public StringCodeGenPipeline createPipeline(GrammarRootAST ast) {
        if (pip.containsKey(ast.getGrammarName()))
            pip.get(ast.getGrammarName());

        LOGGER.debug("create grammar {}", ast.getGrammarName());

        final Grammar g = createGrammar(ast);
        g.fileName = g.name;

        g.loadImportedGrammars();

        StringCodeGenPipeline spip = new StringCodeGenPipeline(g);

        LOGGER.debug("put grammar {}", g.name);

        pip.put(g.name, spip);

        return spip;
    }

    /**
     * get string code generation pipelines in-order (based on imported
     * token vocab)
     *
     * @return ordered set of string code gen pipelines
     */
    public Set<StringCodeGenPipeline> getPipelines() {
        return order.stream()
                .filter(s -> pip.containsKey(s))
                .map(s -> pip.get(s))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * check whether grammar is imported
     *
     * @param name grammar name
     * @return true if grammar is imported, false otherwise
     */
    public boolean isImported(String name) {
        return imported.contains(name);
    }

    /**
     * set parser and lexer variables internally
     *
     * @param g grammar
     */
    private void setParserLexer(Grammar g) {
        String pfx = getPackagePrefix();
        if (g.isParser()) {
            LOGGER.debug("parser {}", g.name);
            parserName = pfx + g.name;
        } else if (g.isLexer()) {
            LOGGER.debug("lexer {}", g.name);
            lexerName = pfx + g.name;
        } else {
            parserName = pfx + g.name + "Parser";
            lexerName = pfx + g.name + "Lexer";
        }
    }

    /**
     * process all code generation pipeline and return the 'main'
     * grammar and lexer names which are used to load the right classes
     * afterwards
     *
     * @return tuple of lexer and parser names
     */
    public Tuple<String, String> process() {
        LOGGER.debug("process grammars");
        StringCodeGenPipeline last = null;
        // order is important here
        Set<StringCodeGenPipeline> pip = getPipelines();

        if (pip.isEmpty())
            throw new IllegalArgumentException("pip must not be empty");

        for (StringCodeGenPipeline p : pip) {
            Grammar g = p.getG();
            LOGGER.debug("process {}", g.name);

            String s = getDepTokVocName(g);
            if (s != null && !s.isEmpty()
                    && tokvok.containsKey(s)
                    && tokvok.get(s) != null) {
                LOGGER.debug("get {}", s);
                String tokvoc = tokvok.get(s);
                if (g instanceof InmemantlrGrammar) {
                    LOGGER.debug("import from {}", tokvoc);
                    ((InmemantlrGrammar) g).setTokenVocab(tokvoc);
                } else if (g instanceof InmemantlrLexerGrammar) {
                    LOGGER.debug("2");
                    ((InmemantlrLexerGrammar) g).setTokenVocab(tokvoc);
                }
            }

            if (!isImported(g.name)) {
                process(p.getG());
                p.process();
                setParserLexer(p.getG());

                if (p.hasTokenVocab()) {
                    LOGGER.debug("put tokvok {}", g.name);
                    tokvok.put(g.name, p.getTokenVocabString());
                }
            }
        }

        if (lexerName.isEmpty())
            throw new IllegalArgumentException("lexerName must not be empty");

        if (parserName.isEmpty())
            throw new IllegalArgumentException("parserName must not be empty");

        return new Tuple<>(parserName, lexerName);
    }

    /**
     * return name of token vocab if imported by grammar g
     *
     * @param g grammar
     * @return name of token vocab
     */
    public String getDepTokVocName(Grammar g) {
        String ret = "";
        GrammarAST tokenVocabNode = findOptionValueAST(g.ast, "tokenVocab");

        if (tokenVocabNode != null) {
            ret = tokenVocabNode.getText();
            LOGGER.debug("TOKENVOC {}", ret);
        }

        return ret;
    }

    /**
     * get compilation units, i.e. all string code generation pipelines that
     * are not imported
     *
     * @return ordered set of string code pipelines
     */
    public Set<StringCodeGenPipeline> getCompilationUnits() {
        return getPipelines().stream()
                .filter(p -> !isImported(p.getG().name))
                .collect(toCollection(LinkedHashSet::new));
    }


    @Override
    public void info(String msg) {
        LOGGER.debug("info");
        super.info(msg);
    }

    public void error(ANTLRMessage msg) {
        LOGGER.debug("error");
        super.error(msg);
    }

    public void warning(ANTLRMessage msg) {
        LOGGER.debug("warning");
        super.warning(msg);

    }

    public void version() {
        this.info("ANTLR Parser Generator  Version " + VERSION);
    }

    public void exit(int e) {
        System.exit(e);
    }

}
