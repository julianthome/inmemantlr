package org.snt.inmemantlr.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.AstProcessorException;

import java.util.List;

public enum AstSerializer {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AstSerializer.class);

    public String toDot(Ast ast) {
        List<AstNode> nodes = ast.getNodes();
        StringBuilder sb = new StringBuilder()
                .append("graph {\n")
                .append("\tnode [fontname=Helvetica,fontsize=11];\n")
                .append("\tedge [fontname=Helvetica,fontsize=10];\n");

        nodes.forEach(n -> sb
                .append("\tn")
                .append(n.getId())
                .append(" [label=\"(")
                .append(n.getId())
                .append(")\\n")
                .append(n.getEscapedLabel())
                .append("\\n")
                .append(n.getRule())
                .append("\"];\n"));

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



}
