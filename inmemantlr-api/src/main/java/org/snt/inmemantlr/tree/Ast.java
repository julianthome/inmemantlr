/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package org.snt.inmemantlr.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

/**
 * an abstract syntax tree
 */
public class Ast {

    private AstNode root = null;
    List<AstNode> nodes = null;

    private Ast() {
        nodes = new Vector<>();
    }

    /**
     * constructor
     *
     * create a new abstract syntax tree
     *
     * @param nt    name of root non-terminal node
     * @param label value of root non-terminal node
     */
    public Ast(String nt, String label) {
        this();
        root = newNode(null, nt, label,0,0);
    }

    /**
     * constructor
     *
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
     *
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
     * @param nt     name of node to be crated
     * @param label  value of node to be created
     * @return newly created node
     */
    public AstNode newNode(AstNode parent, String nt, String label, int sidx,
     int eidx) {
        AstNode rn = new AstNode(this, parent, nt, label, sidx, eidx);
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
        return AstSerializer.INSTANCE.toDot(this);
    }

    /**
     * generate JSON representation from ast
     *
     * @return JSON format string
     */
    public String toJson() {
        return AstSerializer.INSTANCE.toJson(this);
    }

    /**
     * generate XML representation from ast
     *
     * @return XML format string
     */
    public String toXml() {
        return AstSerializer.INSTANCE.toXml(this);
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
            nodes.stream()
                    .filter(n -> oldTree.getRoot().equals(n))
                    .forEach(n -> n.getParent().replaceChild(oldTree.getRoot(), newTree.getRoot()));
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
     * @param n        current root
     * @param selected set to keep track of visited nodes
     * @param p        predicate to search for the dominating subtree root node
     */
    private void searchDominatingNodes(AstNode n, Set<AstNode> selected, Predicate<AstNode> p) {
        if (p.test(n)) {
            selected.add(n);
        } else {
            n.getChildren().forEach(an -> searchDominatingNodes(an, selected, p));
        }
    }

    /**
     * get subtree with the root node identified by p
     *
     * @param p predicate for identifying the root node
     * @return set of ast nodes
     */
    public Set<Ast> getSubtrees(Predicate<AstNode> p) {
        return nodes.stream().filter(p).map(Ast::new).collect(toSet());
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
        if (!(o instanceof Ast))
            return false;

        Ast ast = (Ast) o;
        // will recursively check AST nodes
        return root.equals(ast.getRoot());
    }
}
