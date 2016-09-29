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

import static java.util.stream.Collectors.toSet;

/**
 * An abstract syntax tree
 */
public class Ast {

    private AstNode root = null;
    List<AstNode> nodes = null;

    private Ast() {
        nodes = new Vector<>();
    }

    /**
     * constructor
     * <p>
     * create a new abstract syntax tree
     *
     * @param nt name of root non-terminal node
     * @param label value of root non-terminal node
     */
    public Ast(String nt, String label) {
        this();
        root = newNode(null, nt, label);
    }

    /**
     * constructor
     * <p>
     * copy constructor
     *
     * @param tree tree to be duplicated
     */
    public Ast(Ast tree) {
        this();
        root = newNode(tree.getRoot());
    }

    /**
     * constructor
     * <p>
     *
     * @param nod root node
     */
    private Ast(AstNode nod) {
        this();
        root = newNode(nod);
    }

    /**
     * get root node
     *
     * @return root node
     */
    public AstNode getRoot() {
        return root;
    }

    /**
     * create new ast node
     *
     * @param parent root of new ast node to be created
     * @return newly created ast node
     */
    private AstNode newNode(AstNode parent) {
        AstNode rn = new AstNode(this, parent);
        nodes.add(rn);
        return rn;
    }

    /**
     * create new ast node
     *
     * @param parent parent node
     * @param nt name of node to be crated
     * @param label value of node to be created
     * @return newly created node
     */
    public AstNode newNode(AstNode parent, String nt, String label) {
        AstNode rn = new AstNode(this, parent, nt, label);
        nodes.add(rn);
        return rn;
    }

    /**
     * get leaf nodes
     *
     * @return set of leaf nodes
     */
    public Set<AstNode> getLeafs() {
        return nodes.stream().filter(n -> !n.hasChildren()).collect(toSet());
    }

    /**
     * get all nodes
     *
     * @return list of ast nodes
     */
    public List<AstNode> getNodes() {
        return nodes;
    }

    /**
     * generate dot representation from ast
     *
     * @return dot format string
     */
    public String toDot() {
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

    /**
     * replace oldTree by newTree
     *
     * @param oldTree tree to be replaced
     * @param newTree tree replacement
     * @return true when subtree replacement was successful, false otherwise
     */
    public boolean replaceSubtree(Ast oldTree, Ast newTree) {
        if (hasSubtree(oldTree)) {
            nodes.stream().filter(n -> oldTree.getRoot().equals(n)).forEach(
                    n -> n.getParent().replaceChild(oldTree.getRoot(), newTree.getRoot())
            );
            nodes.addAll(newTree.nodes);
            return nodes.removeAll(oldTree.nodes);
        }
        return false;
    }

    /**
     * remove subtree from ast
     *
     * @param subtree to be removed
     * @return true when removal was succesful, false otherwise
     */
    public boolean removeSubtree(Ast subtree) {
        if (hasSubtree(subtree)) {
            nodes.stream()
                    .filter(n -> subtree.getRoot().equals(n))
                    .forEach(n -> n.getParent().delChild(n));
            return nodes.removeAll(subtree.nodes);
        }
        return false;
    }

    /**
     * find dominant subtrees, i.e., subtrees where the distance of the subtree root
     * node to the ast root node is minimal.
     *
     * @param p predicate to search for the dominating subtree root node
     * @return set of dominating subtrees
     */
    public Set<Ast> getDominatingSubtrees(Predicate<AstNode> p) {
        Set<AstNode> selected = new HashSet<>();
        searchDominatingNodes(root, selected, p);
        return getSubtrees(selected::contains);
    }

    /**
     * helper method for finding the dominating subtrees
     *
     * @param n current root
     * @param selected set to keep track of visited nodes
     * @param p predicate to search for the dominating subtree root node
     */
    private void searchDominatingNodes(AstNode n, Set<AstNode> selected, Predicate<AstNode> p) {
        if (p.test(n)) {
            selected.add(n);
        } else {
            for (AstNode an : n.getChildren()) {
                searchDominatingNodes(an, selected, p);
            }
        }
    }

    /**
     * get subtree with the root node identified by p
     *
     * @param p predicate for identifying the root node
     * @return set of ast nodes
     */
    public Set<Ast> getSubtrees(Predicate<AstNode> p) {
        Set<Ast> ret = new HashSet<>();

        nodes.stream()
                .filter(p)
                .forEach(n -> {
                    Ast a = new Ast(n);
                    ret.add(a);
                });
        return ret;
    }

    /**
     * check the presence of subree in the current tree
     *
     * @param subtree tree whose presence is checked
     * @return true if subtree is present in actual one, false otherwise
     */
    public boolean hasSubtree(Ast subtree) {
        Set<Ast> subtrees = getSubtrees(n -> subtree.getRoot().equals(n));
        return subtrees.stream().filter(subtree::equals).count() > 0;
    }

    /**
     * get subtree
     *
     * @param subtree tree whose presence is checked
     * @return subree
     */
    public Ast getSubtree(Ast subtree) {
        Set<Ast> subtrees = getSubtrees(n -> n.equals(subtree.getRoot()));
        return subtrees.stream().filter(subtree::equals).findFirst().orElse(null);
    }

    @Override
    public int hashCode() {
        return root.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ast)) {
            return false;
        }
        Ast ast = (Ast) o;
        // will recursively check AST nodes
        return root.equals(ast.getRoot());
    }
}
