package org.snt.inmemantlr.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.ParseTreeProcessorException;

import java.util.List;

public enum ParseTreeSerializer {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseTreeSerializer.class);

    public String toDot(ParseTree parseTree, boolean rulesOnly) {
        List<ParseTreeNode> nodes = parseTree.getNodes();
        StringBuilder sb = new StringBuilder()
                .append("graph {\n")
                .append("\tnode [fontname=Helvetica,fontsize=11];\n")
                .append("\tedge [fontname=Helvetica,fontsize=10];\n");

        for(ParseTreeNode n : nodes ) {
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
                .filter(ParseTreeNode::hasParent)
                .forEach(c -> sb
                        .append("\tn")
                        .append(c.getParent().getId())
                        .append(" -- n")
                        .append(c.getId())
                        .append(";\n")));

        sb.append("}\n");

        return sb.toString();
    }

    public String toDot(ParseTree parseTree) {
        return toDot(parseTree, false);
    }

    public String toJson(ParseTree parseTree) {
        JsonProcessor jsonProc = new JsonProcessor(parseTree);
        StringBuilder sb = null;
        try {
            sb = jsonProc.process();
        } catch (ParseTreeProcessorException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return sb.toString();
    }


    public String toXml(ParseTree parseTree) {
        XmlProcessor xmlProc = new XmlProcessor(parseTree);
        StringBuilder sb = null;
        try {
            sb = xmlProc.process();
        } catch (ParseTreeProcessorException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return sb.toString();
    }




}
