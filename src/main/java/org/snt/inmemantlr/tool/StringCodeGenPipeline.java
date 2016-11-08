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

import org.antlr.v4.codegen.CodeGenPipeline;
import org.antlr.v4.codegen.CodeGenerator;
import org.antlr.v4.codegen.model.*;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.ast.GrammarAST;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.stringtemplate.v4.ST;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * extended code gen pipeline for compiling
 * antlr grammars in-memory
 */
public class StringCodeGenPipeline extends CodeGenPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringCodeGenPipeline.class);

    private Grammar g;
    private String name;

    private ST parser, lexer, visitor, listener, baseListener, baseVisitor;
    private ST tokenvocab;


    /**
     * constructor
     *
     * @param g antlr grammar object
     * @param name grammar name
     */
    public StringCodeGenPipeline(Grammar g, String name) {
        super(g);
        this.g = g;
        this.name = name;
        lexer = null;
        listener = null;
        visitor = null;
        baseListener = null;
        baseVisitor = null;
    }

    /**
     * check if parser is set
     *
     * @return true if parser is set, false otherwise
     */
    public boolean hasParser() {
        return parser != null;
    }

    /**
     * get parser
     *
     * @return parser
     */
    public ST getParser() {
        return parser;
    }

    /**
     * get base listener
     *
     * @return base listener
     */
    public ST getBaseListener() {
        return baseListener;
    }

    /**
     * check if base listener is set
     *
     * @return true if listener is set, false otherwise
     */
    public boolean hasBaseListener() {
        return baseListener != null;
    }

    /**
     * get lexer
     *
     * @return lexer
     */
    public ST getLexer() {
        return lexer;
    }

    /**
     * check if lexer is set
     *
     * @return true if lexer is set, false otherwise
     */
    public boolean hasLexer() {
        return lexer != null;
    }

    /**
     * get visitor
     *
     * @return visitor
     */
    public ST getVisitor() {
        return visitor;
    }

    /**
     * check if visitor is set
     *
     * @return true if visitor is set, false otherwise
     */
    public boolean hasVisitor() {
        return visitor != null;
    }

    /**
     * get listener
     *
     * @return listener
     */
    public ST getListener() {
        return listener;
    }

    /**
     * check if listener is set
     *
     * @return true if listener is set, false otherwise
     */
    public boolean hasListener() {
        return listener != null;
    }

    /**
     * get base visitor
     *
     * @return base visitor
     */
    public ST getBaseVisitor() {
        return baseVisitor;
    }

    public ST getTokenVocab() {
        return this.tokenvocab;
    }

    /**
     * check if visitor is set
     *
     * @return true if visitor is set, false otherwise
     */
    public boolean hasBaseVisitor() {
        return baseVisitor != null;
    }


    public boolean hasTokenVocab() {
        return this.tokenvocab != null;
    }

    /**
     * get grammar
     *
     * @return grammar
     */
    public Grammar getG() {
        return g;
    }

    /**
     * set grammar
     *
     * @param g grammar
     */
    public void setG(InmemantlrGrammar g) {
        this.g = g;
    }

    /**
     * compile lexer and parser
     */
    public void process() {

        CodeGenerator cgen = new CodeGenerator(g);
        IntervalSet idTypes = new IntervalSet();
        idTypes.add(ANTLRParser.ID);
        idTypes.add(ANTLRParser.RULE_REF);
        idTypes.add(ANTLRParser.TOKEN_REF);

        List<GrammarAST> idNodes = g.ast.getNodesWithType(idTypes);
        idNodes.stream()
                .filter(idNode -> cgen.getTarget()
                        .grammarSymbolCausesIssueInGeneratedCode(idNode))
                .forEach(idNode -> g.tool.errMgr.grammarError(ErrorType.USE_OF_BAD_WORD,
                        g.fileName, idNode.getToken(),
                        idNode.getText()));

        if (g.isLexer()) {
            lexer = cgen.generateLexer();
        } else {
            parser = cgen.generateParser();

            if (g.tool.gen_listener) {
                listener = cgen.generateListener();
                if (cgen.getTarget().wantsBaseListener()) {
                    baseListener = cgen.generateBaseListener();
                }
            }
            if (g.tool.gen_visitor) {
                visitor = cgen.generateVisitor();
                if (cgen.getTarget().wantsBaseVisitor()) {
                    baseVisitor = cgen.generateBaseVisitor();
                }
            }

            LexerGrammar lg;
            if ((lg = g.implicitLexer) != null) {
                CodeGenerator lgcg = new CodeGenerator(lg);
                lexer = lgcg.generateLexer();
            }

        }

        tokenvocab = getTokenVocabOutput();
    }

    ST getTokenVocabOutput() {
        ST vocabFileST = new ST(CodeGenerator.vocabFilePattern);
        Map<String,Integer> tokens = new LinkedHashMap<String,Integer>();
        // make constants for the token names
        for (String t : g.tokenNameToTypeMap.keySet()) {
            int tokenType = g.tokenNameToTypeMap.get(t);
            if ( tokenType>= Token.MIN_USER_TOKEN_TYPE) {
                tokens.put(t, tokenType);
            }
        }
        vocabFileST.add("tokenvocab", tokens);

        // now dump the strings
        Map<String,Integer> literals = new LinkedHashMap<String,Integer>();
        for (String literal : g.stringLiteralToTypeMap.keySet()) {
            int tokenType = g.stringLiteralToTypeMap.get(literal);
            if ( tokenType>=Token.MIN_USER_TOKEN_TYPE) {
                literals.put(literal, tokenType);
            }
        }
        vocabFileST.add("literals", literals);

        return vocabFileST;
    }


    /**
     * get parser name
     *
     * @return parser name
     */
    public String getParserName() {
        ParserFile f = (ParserFile)this.parser.getAttributes().get("file");
        LOGGER.debug("parser name {}", modFile(f));
        return modFile(f);
    }

    /**
     * get lexer name
     *
     * @return lexer name
     */
    public String getLexerName() {
        LexerFile f = (LexerFile)this.lexer.getAttributes().get("lexerFile");
        LOGGER.debug("lexer name {}", modFile(f));
        return modFile(f);
    }

    /**
     * get visitor name
     *
     * @return visitor name
     */
    public String getVisitorName() {
        VisitorFile f = (VisitorFile) this.listener.getAttributes().get
                ("file");
        LOGGER.debug("listener name {}", modFile(f));
        return modFile(f);
    }

    /**
     * get base visitor name
     *
     * @return base visitor name
     */
    public String getBaseVisitorName() {
        BaseVisitorFile f = (BaseVisitorFile)this.listener.getAttributes().get
                ("file");
        LOGGER.debug("listener name {}", modFile(f));
        return modFile(f);
    }

    /**
     * get listener name
     *
     * @return listener name
     */
    public String getListenerName() {
        ListenerFile f = (ListenerFile)this.listener.getAttributes().get
                ("file");
        LOGGER.debug("listener name {}", modFile(f));
        return modFile(f);
    }

    /**
     * get base listener name
     *
     * @return base listener name
     */
    public String getBaseListenerName() {
        BaseListenerFile f = (BaseListenerFile)this.baseListener.getAttributes().get("file");
        LOGGER.debug("base listener name {}", modFile(f));
        return modFile(f);
    }


    public String getTokenVocabFileName() {
        return this.g.name + CodeGenerator.VOCAB_FILE_EXTENSION;
    }

    public String getTokenVocabString() {


        Map<String,Integer> vocab = (Map<String,Integer>)tokenvocab.getAttribute
                ("tokenvocab");
        Map<String,Integer> lit = (Map<String,Integer>)tokenvocab.getAttribute
                ("literals");

        StringBuilder sb = new StringBuilder();

        vocab.forEach(
                (s,i) -> sb.append(s.toString() + "=" + i.toString() +  "\n")
        );

        lit.forEach(
                (s,i) -> sb.append(s.toString() + "=" + i.toString() +  "\n")
        );

        return sb.toString();
    }

    private String modFile(OutputFile f) {
        return FilenameUtils.removeExtension(f.fileName);
    }
}
