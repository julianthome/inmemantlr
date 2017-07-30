package org.snt.inmemantlr.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.AstProcessorException;

import java.util.List;

public enum AstSerializer {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AstSerializer.class);

    public String toDot(Ast ast, boolean rulesOnly) {
        List<AstNode> nodes = ast.getNodes();
        StringBuilder sb = new StringBuilder()
                .append("graph {\n")
                .append("\tnode [fontname=Helvetica,fontsize=11];\n")
                .append("\tedge [fontname=Helvetica,fontsize=10];\n");

        for(AstNode n : nodes ) {
            sb.append("\tn");
            sb.append(n.getId());
            sb.append(" [label=\"");
            if(!rulesOnly) {
                sb.append("(");
                sb.append(n.getId());
                sb.append(")\\n");
                sb.append(n.getEscapedLabel());
                sb.append("\\n");
            }
            sb.append(n.getRule());
            sb.append("\"];\n");
        }

        nodes.forEach(n -> n.getChildren().stream()
                .filter(AstNode::hasParent)
                .forEach(c -> sb
                        .append("\tn")
                        .append(c.getParent().getId())
                        .append(" -- n")
                        .append(c.getId())
                        .append(";\n")));

        sb.append("}\n");

        return sb.toString();
    }

    public String toDot(Ast ast) {
        return toDot(ast, false);
    }

    public String toJson(Ast ast) {
        JsonProcessor jsonProc = new JsonProcessor(ast);
        StringBuilder sb = null;
        try {
            sb = jsonProc.process();
        } catch (AstProcessorException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return sb.toString();
    }


    public String toXml(Ast ast) {
        XmlProcessor xmlProc = new XmlProcessor(ast);
        StringBuilder sb = null;
        try {
            sb = xmlProc.process();
        } catch (AstProcessorException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return sb.toString();
    }




}
