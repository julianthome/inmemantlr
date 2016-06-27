package org.snt.inmemantlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.tree.AstNode;
import org.snt.inmemantlr.tree.NodeFilter;

import java.util.Set;
import java.util.Stack;

public class DefaultTreeListener extends DefaultListener {

    private Stack<String> sctx = new Stack<String>();

    private StringBuffer glob = new StringBuffer();

    private Ast ast = null;
    private AstNode nodeptr = null;
    private NodeFilter filter = null;


    public DefaultTreeListener(NodeFilter ntype) {
        this.sctx.add("S");
        this.ast = new Ast();
        this.nodeptr = this.ast.getRoot();
        this.filter = ntype;
    }


    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        String rule = this.getRuleByKey(ctx.getRuleIndex());

        if(this.filter.isEmpty() || this.filter.hasType(rule)){
            AstNode n = ast.newNode(nodeptr, rule, ctx.getText());
            nodeptr.addChild(n);
            nodeptr = n;
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        String rule = this.getRuleByKey(ctx.getRuleIndex());
        if(this.filter.isEmpty() || this.filter.hasType(rule)){
            this.nodeptr = this.nodeptr.getParent();
        }
    }

    public Ast getAst() {
        return this.ast;
    }

    public Set<AstNode> getNodes() {
        return this.getNodes();
    }

    @Override
    public String toString() {
        return this.glob.toString();
    }


}
