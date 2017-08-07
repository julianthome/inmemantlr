package org.snt.inmemantlr.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.AstProcessorException;

public class JsonProcessor extends AstProcessor<StringBuilder, StringBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonProcessor.class);

    private boolean idxOnly = false;

    /**
     * constructor
     *
     * @param ast abstract syntax tree to process
     * @param idxOnly print index only
     */
    public JsonProcessor(Ast ast, boolean idxOnly) {
        super(ast);
        this.idxOnly = idxOnly;
    }

    /**
     * constructor
     *
     * @param ast abstract syntax tree to process
     */
    public JsonProcessor(Ast ast) {
        this(ast,true);
    }

    @Override
    public StringBuilder getResult() {
        return smap.get(ast.getRoot());
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void process(AstNode n) throws AstProcessorException {

        StringBuilder sb = new StringBuilder();

        if(!n.hasParent()){
            simpleProp(n);
            return;
        }

        sb.append("{");
        sb.append("\"nt\":\"");
        sb.append(n.getRule());
        sb.append("\",\"ran\":\"");
        sb.append(n.getSidx());
        sb.append(",");
        sb.append(n.getEidx());
        sb.append("\"");

        if(!idxOnly) {
            sb.append("\",\"lbl\":\"");
            sb.append(n.getEscapedLabel());
            sb.append("\"");
        }

        if (n.hasChildren()) {
            sb.append(",");
            sb.append("\"cld\":[");
            for (int i = 0; i < n.getChildren().size(); i++) {
                if (i > 0)
                    sb.append(",");

                sb.append(smap.get(n.getChild(i)));
            }
            sb.append("]");
        }

        sb.append("}");

        smap.put(n, sb);
    }
}
