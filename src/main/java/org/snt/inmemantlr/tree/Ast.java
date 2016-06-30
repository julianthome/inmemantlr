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

package org.snt.inmemantlr.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Ast {

    private AstNode root = null;
    List<AstNode> nodes = null;

    private Ast() {
        this.nodes = new Vector<AstNode>();
    }

    public Ast(String nt, String label) {
        this();
        this.root = newNode(null,nt,label);
    }


    public Ast(Ast tree) {
        this();
        this.root = newNode(tree.getRoot());
    }

    private Ast(AstNode nod) {
        this();
        this.root = newNode(nod);
    }

    public AstNode getRoot() {
        return root;
    }

    private AstNode newNode(AstNode nod){
        AstNode rn = new AstNode(this,nod);
        this.nodes.add(rn);
        return rn;
    }

    public AstNode newNode(AstNode parent, String nt, String label){
        AstNode rn = new AstNode(this,parent,nt,label);
        this.nodes.add(rn);
        return rn;
    }

    public Set<AstNode> getLeafs() {
        return this.nodes.stream().filter(n -> !n.hasChildren()).
                collect(Collectors.toSet());
    }


    public List<AstNode> getNodes() {
        return this.nodes;
    }


    public String toDot() {
        StringBuilder sb = new StringBuilder();

        sb.append("graph {\n");

        sb.append("\tnode [fontname=Helvetica,fontsize=11];\n");
        sb.append("\tedge [fontname=Helvetica,fontsize=10];\n");

        this.nodes.forEach(
                n -> sb.append("\tn" + n.getId() + " [label=\"(" +n.getId() +")\\n"  +
                        n.getEscapedLabel() + "\\n" + n.getRule().toString() + "\"];\n"));

        this.nodes.forEach(n -> n.getChildren().stream().filter(c -> c.hasParent()).forEach(c-> sb.append("\tn" +
                c.getParent().getId() + " -- n" + c.getId() + ";\n")));

        sb.append("}\n");

        return sb.toString();
    }


    public boolean replaceSubtree(Ast oldTree, Ast newTree) {

        if (this.hasSubtree(oldTree)) {
            this.nodes.stream().filter(n -> oldTree.getRoot().equals(n)).forEach(
                    n -> {
                        n.getParent().replaceChild(oldTree.getRoot(),newTree.getRoot());
                    }
            );
            this.nodes.addAll(newTree.nodes);
            return this.nodes.removeAll(oldTree.nodes);
        }
        return false;
    }


    public boolean removeSubtree(Ast subtree) {
        if (this.hasSubtree(subtree)) {
            this.nodes.stream().filter(n -> subtree.getRoot().equals(n)).forEach(
                  n -> {
                      n.getParent().delChild(n);
                  }
            );
            return this.nodes.removeAll(subtree.nodes);
        }
        return false;
    }

    public Set<Ast> getDominatingSubtrees(Predicate<AstNode> p) {
        Set<AstNode> selected = new HashSet<>();
        searchDominatingNodes(this.root,selected,p);
        System.out.println(selected);
        return getSubtrees(n -> selected.contains(n));
    }

    private void searchDominatingNodes(AstNode n, Set<AstNode> selected, Predicate<AstNode> p) {
        if(p.test(n)) {
            selected.add(n);
        } else {
            for (AstNode an : n.getChildren()) {
                searchDominatingNodes(an, selected,p);
            }
        }
    }

    public Set<Ast> getSubtrees(Predicate<AstNode> p) {

        Set<Ast> ret = new HashSet<Ast>();

        this.nodes.stream().filter(p).forEach(
                n-> {
                    Ast a = new Ast(n);
                    ret.add(a);
                }
        );
        return ret;
    }

    public boolean hasSubtree(Ast subtree) {
        Set<Ast> subtrees = getSubtrees(n -> subtree.getRoot().equals(n));
        return subtrees.stream().filter(s -> subtree.equals(s)).count() > 0;
    }

    public Ast getSubtree(Ast subtree) {
        Set<Ast> subtrees = getSubtrees(n -> n.equals(subtree.getRoot()));
        return subtrees.stream().filter(s -> subtree.equals(s)).findFirst().get();
    }


    @Override
    public int hashCode() {
        return this.root.getId();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Ast)) {
            return false;
        }
        Ast ast = (Ast)o;
        // will recursively check AST nodes
        return this.root.equals(ast.getRoot());
    }

}
