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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
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
            AstNode n = ast.newNode(nodeptr, rule, ctx.getText());
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
