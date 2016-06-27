package org.snt.inmemantlr.tree;

import java.util.HashSet;
import java.util.Set;

public class Ast {

    private AstNode root = null;

    private Set<AstNode> nodes = null;

    public Ast() {
        this.nodes = new HashSet<AstNode>();
        this.root = new AstNode(null,"root","root");
        this.nodes.add(this.root);
    }

    public AstNode getRoot() {
        return root;
    }

    public AstNode newNode(AstNode parent, String nt, String label){
        AstNode rn = new AstNode(parent,nt,label);
        this.nodes.add(rn);
        return rn;
    }


    public Set<AstNode> getLeafs() {

        Set<AstNode> leafs = new HashSet<AstNode>();
        for(AstNode rn : this.nodes) {
            if(!rn.hasChildren()){
                leafs.add(rn);
            }
        }

        return leafs;
    }


    public Set<AstNode> getNodes() {
        return this.nodes;
    }


    public String toDot() {
        StringBuilder sb = new StringBuilder();

        sb.append("graph {\n");

        for(AstNode rn : this.nodes) {
            sb.append("\tn" + rn.getId() + " [label=\"(" +rn.getId() +")\\n"  + rn.getLabel() + "\\n" + rn.getType().toString() + "\"];\n");
        }

        for(AstNode rn : this.nodes) {
            for(AstNode c : rn.getChildren()) {
                sb.append("\tn" + rn.getId() + " -- n" + c.getId() + ";\n");
            }
        }

        sb.append("}\n");

        return sb.toString();
    }






}
