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
 * extended code gen pipeline for compiling
 * antlr grammars in-memory
 */
public class StringCodeGenPipeline extends CodeGenPipeline {

    Grammar g;
    String name;
    private ST parser, lexer, visitor, listener, baseListener, baseVisitor;

    /**
     * constructor
     * @param g antlr grammar object
     * @param name grammar name
     */
    public StringCodeGenPipeline(Grammar g, String name) {
        super(g);
        this.g = g;
        this.name = name;
        this.lexer = null;
        this.listener = null;
        this.visitor = null;
        this.baseListener = null;
        this.baseVisitor = null;
    }

    /**
     * check if parser is set
     * @return true if parser is set, false otherwise
     */
    public boolean hasParser() {
        return this.parser != null;
    }

    /**
     * get parser
     * @return parser
     */
    public ST getParser() {
        return this.parser;
    }

    /**
     * get base listener
     * @return base listener
     */
    public ST getBaseListener() {
        return this.baseListener;
    }

    /**
     * check if base listener is set
     * @return true if listener is set, false otherwise
     */
    public boolean hasBaseListener() {
        return this.baseListener != null;
    }

    /**
     * get lexer
     * @return lexer
     */
    public ST getLexer() {
        return this.lexer;
    }

    /**
     * check if lexer is set
     * @return true if lexer is set, false otherwise
     */
    public boolean hasLexer() {
        return this.lexer != null;
    }

    /**
     * get visitor
     * @return visitor
     */
    public ST getVisitor() {
        return visitor;
    }

    /**
     * check if visitor is set
     * @return true if visitor is set, false otherwise
     */
    public boolean hasVisitor() {
        return this.visitor != null;
    }

    /**
     * get listener
     * @return listener
     */
    public ST getListener() {
        return this.listener;
    }

    /**
     * check if listener is set
     * @return true if listener is set, false otherwise
     */
    public boolean hasListener() {
        return this.listener != null;
    }

    /**
     * get base visitor
     * @return base visitor
     */
    public ST getBaseVisitor() {
        return this.baseVisitor;
    }

    /**
     * check if visitor is set
     * @return true if visitor is set, false otherwise
     */
    public boolean hasBaseVisitor() {
        return this.baseVisitor != null;
    }

    /**
     * get grammar
     * @return grammar
     */
    public Grammar getG() {
        return g;
    }

    /**
     * set grammar
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
        for (GrammarAST idNode : idNodes) {
            if (gen.getTarget().grammarSymbolCausesIssueInGeneratedCode(idNode)) {
                g.tool.errMgr.grammarError(ErrorType.USE_OF_BAD_WORD,
                        g.fileName, idNode.getToken(),
                        idNode.getText());
            }
        }

        if (g.isLexer()) {
            this.lexer = gen.generateLexer();
        } else {
            this.parser = gen.generateParser();

            if (g.tool.gen_listener) {
                this.listener = gen.generateListener();
                if (gen.getTarget().wantsBaseListener()) {
                    this.baseListener = gen.generateBaseListener();
                }
            }
            if (g.tool.gen_visitor) {
                this.visitor = gen.generateVisitor();
                if (gen.getTarget().wantsBaseVisitor()) {
                    this.baseVisitor = gen.generateBaseVisitor();
                }
            }

            LexerGrammar lg = null;
            if ((lg = g.implicitLexer) != null) {

                CodeGenerator lgcg = new CodeGenerator(lg);
                this.lexer = lgcg.generateLexer();
            }

        }

    }


    /**
     * get parser name
     * @return parser name
     */
    public String getParserName() {
        return this.name + "Parser";
    }

    /**
     * get lexer name
     * @return lexer name
     */
    public String getLexerName() {
        return this.name + "Lexer";
    }

    /**
     * get visitor name
     * @return visitor name
     */
    public String getVisitorName() {
        return this.name + "Visitor";
    }

    /**
     * get base visitor name
     * @return base visitor name
     */
    public String getBaseVisitorName() {
        return this.name + "BaseVisitor";
    }

    /**
     * get listener name
     * @return listener name
     */
    public String getListenerName() {
        return this.name + "Listener";
    }

    /**
     * get base listener name
     * @return base listener name
     */
    public String getBaseListenerName() {
        return this.name + "BaseListener";
    }


}
