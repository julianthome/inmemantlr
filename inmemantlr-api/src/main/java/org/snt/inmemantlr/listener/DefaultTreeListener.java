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

package org.snt.inmemantlr.listener;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.tree.AstNode;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * default tree listener implementation
 */
public class DefaultTreeListener extends DefaultListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTreeListener.class);

    private static final long serialVersionUID = 5637734678821255670L;

    private Stack<String> sctx = new Stack<>();
    private StringBuffer glob = new StringBuffer();
    private Ast ast = null;
    private AstNode nodeptr = null;
    private Predicate<String> filter = null;

    /**
     * constructor
     */
    public DefaultTreeListener() {
        this(x -> !x.isEmpty());
    }

    /**
     * constructor
     *
     * @param filter condition that has to hold for every node
     */
    public DefaultTreeListener(Predicate<String> filter) {
        sctx.add("S");
        ast = new Ast("root", "root");
        nodeptr = ast.getRoot();
        this.filter = filter;
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        String rule = getRuleByKey(ctx.getRuleIndex());
        if (filter.test(rule)) {
            Token s = ctx.getStart();
            Token e = ctx.getStop();
            AstNode n = ast.newNode(nodeptr, rule, ctx.getText(),
                    s != null ? s.getStartIndex() : 0,
                    e != null ? e.getStopIndex() : 0);
            nodeptr.addChild(n);
            nodeptr = n;
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        String rule = getRuleByKey(ctx.getRuleIndex());
        if (filter.test(rule)) {
            nodeptr = nodeptr.getParent();
        }
    }

    @Override
    public void reset() {
        super.reset();
        sctx.clear();
        sctx.add("S");
        ast = new Ast("root", "root");
        nodeptr = ast.getRoot();
        glob.delete(0, glob.length());
    }

    /**
     * get ast
     *
     * @return ast
     */
    public Ast getAst() {
        return ast;
    }

    /**
     * get ast nodes
     *
     * @return set of ast nodes
     */
    public Set<AstNode> getNodes() {
        return new HashSet<>(ast.getNodes());
    }

    @Override
    public String toString() {
        return glob.toString();
    }
}
