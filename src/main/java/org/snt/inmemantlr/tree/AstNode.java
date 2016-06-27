package org.snt.inmemantlr.tree;


import org.snt.inmemantlr.utils.EscapeUtils;

import java.util.List;
import java.util.Vector;

public class AstNode {

    private String label;
    private String ntype;
    private AstNode parent;

    private static int idx = 0;
    private int id = idx ++;

    private List<AstNode> children;

    protected AstNode(AstNode parent, String nt, String label) {
        this.children = new Vector<AstNode>();
        this.ntype = nt;
        this.label = label;
        this.parent = parent;
    }

    public AstNode getLastChild(){
        if(this.children.size() > 0) {
            this.children.get(this.children.size()-1);
        }
        return null;
    }

    public AstNode getParent() {
        return this.parent;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public List<AstNode> getChildren() {
        return this.children;
    }

    public void addChild(AstNode n) {
        this.children.add(n);
    }

    public int getId() {
        return this.id;
    }

    public String getType() {
        return this.ntype;
    }

    public String getLabel() {
        return EscapeUtils.escapeSpecialCharacters(this.label);
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.ntype.toString() + " " + this.label;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AstNode))
            return false;

        return ((AstNode)o).id == this.id;

    }


    public boolean isLeaf() {
        return this.children.size() == 0;
    }


}
