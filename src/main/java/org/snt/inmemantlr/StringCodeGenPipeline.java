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

package org.snt.inmemantlr;

import org.antlr.v4.codegen.CodeGenPipeline;
import org.antlr.v4.codegen.CodeGenerator;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.ast.GrammarAST;
import org.stringtemplate.v4.ST;

import java.util.List;

/**
 * xtended code gen pipeline for compiling
 * antlr grammars in-memory
 */
public class StringCodeGenPipeline extends CodeGenPipeline {

    Grammar g;
    String name;
    private ST parser, lexer, visitor, listener, baseListener, baseVisitor;

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

    /**
     * check if visitor is set
     *
     * @return true if visitor is set, false otherwise
     */
    public boolean hasBaseVisitor() {
        return baseVisitor != null;
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
    public void setG(Grammar g) {
        this.g = g;
    }

    /**
     * compile lexer and parser
     */
    public void process() {
        CodeGenerator gen = new CodeGenerator(g);
        IntervalSet idTypes = new IntervalSet();
        idTypes.add(ANTLRParser.ID);
        idTypes.add(ANTLRParser.RULE_REF);
        idTypes.add(ANTLRParser.TOKEN_REF);
        List<GrammarAST> idNodes = g.ast.getNodesWithType(idTypes);
        idNodes.stream()
                .filter(idNode -> gen.getTarget().grammarSymbolCausesIssueInGeneratedCode(idNode))
                .forEach(idNode -> g.tool.errMgr.grammarError(ErrorType.USE_OF_BAD_WORD,
                        g.fileName, idNode.getToken(),
                        idNode.getText()));

        if (g.isLexer()) {
            lexer = gen.generateLexer();
        } else {
            parser = gen.generateParser();

            if (g.tool.gen_listener) {
                listener = gen.generateListener();
                if (gen.getTarget().wantsBaseListener()) {
                    baseListener = gen.generateBaseListener();
                }
            }
            if (g.tool.gen_visitor) {
                visitor = gen.generateVisitor();
                if (gen.getTarget().wantsBaseVisitor()) {
                    baseVisitor = gen.generateBaseVisitor();
                }
            }

            LexerGrammar lg;
            if ((lg = g.implicitLexer) != null) {
                CodeGenerator lgcg = new CodeGenerator(lg);
                lexer = lgcg.generateLexer();
            }
        }
    }

    /**
     * get parser name
     *
     * @return parser name
     */
    public String getParserName() {
        return name + "Parser";
    }

    /**
     * get lexer name
     *
     * @return lexer name
     */
    public String getLexerName() {
        return name + "Lexer";
    }

    /**
     * get visitor name
     *
     * @return visitor name
     */
    public String getVisitorName() {
        return name + "Visitor";
    }

    /**
     * get base visitor name
     *
     * @return base visitor name
     */
    public String getBaseVisitorName() {
        return name + "BaseVisitor";
    }

    /**
     * get listener name
     *
     * @return listener name
     */
    public String getListenerName() {
        return name + "Listener";
    }

    /**
     * get base listener name
     *
     * @return base listener name
     */
    public String getBaseListenerName() {
        return name + "BaseListener";
    }
}
