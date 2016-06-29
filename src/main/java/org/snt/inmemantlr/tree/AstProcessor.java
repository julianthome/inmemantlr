package org.snt.inmemantlr.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public abstract class AstProcessor<R,T> {

    protected Ast ast = null;


    private Map<AstNode, Integer> nmap;
    protected Map<AstNode, T> smap;
    protected LinkedList<AstNode> active;

    public AstProcessor(Ast ast) {
        this.ast = ast;
        this.nmap = new HashMap<AstNode, Integer>();
        this.smap = new HashMap<AstNode, T>();
        this.active = new LinkedList<AstNode>();
    }

    public R process() {

        initialize();

        for (AstNode rn : ast.getNodes()) {
            nmap.put(rn, rn.getChildren().size());
        }

        //logger.info(debug());
        for (AstNode l : ast.getLeafs()) {
            active.addFirst(l);
        }

        while (!active.isEmpty()) {
            AstNode rn = active.pollFirst();

            process(rn);

            AstNode parent = rn.getParent();

            if(parent != null) {
                nmap.replace(parent, nmap.get(parent) - 1);
                if (nmap.get(parent) == 0) {
                    active.addLast(parent);
                }
            }

        }
        return getConstraints();
    }

    public String debug() {

        StringBuilder sb = new StringBuilder();

        sb.append(".....Smap......\n");
        for (Map.Entry<AstNode, T> e : smap.entrySet()) {
            sb.append(e.getKey().getId() + " :: " + e.getValue() + "\n");
        }

        sb.append(".....Nmap......\n");

        for (Map.Entry<AstNode, Integer> e : nmap.entrySet()) {
            sb.append(e.getKey().getId() + " :: " + e.getValue() + "\n");
        }

        return sb.toString();
    }

    public abstract R getConstraints();
    protected abstract void initialize();
    protected abstract void process(AstNode n);

}
