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

import org.snt.inmemantlr.utils.EscapeUtils;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {

    private String label;
    private String ntype;
    private ParseTreeNode parent;
    private ParseTree tree;
    private int id;

    private int sidx = 0;
    private int eidx = 0;

    private int line = 0;
    private int charPositionInLine = 0;

    private List<ParseTreeNode> children;
    private static int cnt = 0;

    /**
     * constructor
     *
     * @param tree tree to whom the node belongs to
     */
    private ParseTreeNode(ParseTree tree) {
        this.tree = tree;
        id = cnt++;
        children = new ArrayList<>();
    }

    /**
     * constructor
     *
     * @param tree               tree to whom the node belongs to
     * @param parent             parent node
     * @param nt                 non terminal id
     * @param sidx               start index
     * @param eidx               end index
     * @param label              label
     * @param line               line
     * @param charPositionInLine character position in line
     */
    protected ParseTreeNode(ParseTree tree, ParseTreeNode parent, String nt, String label, int
            sidx, int eidx, int line, int charPositionInLine) {
        this(tree);
        ntype = nt;
        this.label = label;
        this.parent = parent;
        this.sidx = sidx;
        this.eidx = eidx;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    /**
     * deep copy constructor
     *
     * @param tree tree to whom the node belongs to
     * @param nod  node to duplication
     */
    protected ParseTreeNode(ParseTree tree, ParseTreeNode nod) {
        this(tree);
        id = nod.id;
        ntype = nod.ntype;
        label = nod.label;
        this.eidx = nod.eidx;
        this.sidx = nod.sidx;
        this.line = nod.line;
        this.charPositionInLine = nod.charPositionInLine;
        for (ParseTreeNode c : nod.children) {
            ParseTreeNode cnod = new ParseTreeNode(tree, c);
            cnod.parent = this;
            this.tree.nodes.add(cnod);
            children.add(cnod);
        }
    }

    /**
     * get child with index i
     *
     * @param i the index of the child
     * @return child with index i
     */
    public ParseTreeNode getChild(int i) {
        if (i < 0 || i > children.size())
            throw new IllegalArgumentException("Index must be greater than or equal to zero and less than the children size");

        return children.get(i);
    }

    /**
     * get last child (note that nodes are ordered in their appearance)
     *
     * @return last child
     */
    public ParseTreeNode getLastChild() {
        if (!children.isEmpty()) {
            return children.get(children.size() - 1);
        }
        return null;
    }

    /**
     * get first child (note that nodes are ordered in their appearance)
     *
     * @return first child
     */
    public ParseTreeNode getFirstChild() {
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return null;
    }

    /**
     * set parent node
     *
     * @param par parent node
     */
    public void setParent(ParseTreeNode par) {
        parent = par;
    }

    /**
     * check if node has parent
     *
     * @return true if node has parent, false otherwise
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * get parent node
     *
     * @return parent node
     */
    public ParseTreeNode getParent() {
        return parent;
    }

    /**
     * check if node has children
     *
     * @return true if node has children, false otherwise
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * get List of children
     *
     * @return list of children
     */
    public List<ParseTreeNode> getChildren() {
        return children;
    }

    /**
     * append child node
     *
     * @param n child node to be added
     */
    public void addChild(ParseTreeNode n) {
        children.add(n);
    }

    /**
     * delete child node
     *
     * @param n child node to be deleted
     */
    public void delChild(ParseTreeNode n) {
        children.remove(n);
    }

    /**
     * replace child
     *
     * @param oldNode child to be replaced
     * @param newNode replacement
     */
    public void replaceChild(ParseTreeNode oldNode, ParseTreeNode newNode) {
        if (children.contains(oldNode)) {
            children.set(children.indexOf(oldNode), newNode);
            newNode.parent = this;
        }
    }

    /**
     * gt identifier
     *
     * @return id which identifies node uniquely
     */
    public int getId() {
        return id;
    }

    /**
     * get non-terminal rule of that node
     *
     * @return non-terminal rule
     */
    public String getRule() {
        return ntype;
    }

    /**
     * get label where special chars are escaped
     *
     * @return escaped label
     */
    public String getEscapedLabel() {
        return EscapeUtils.escapeSpecialCharacters(label);
    }

    /**
     * get start index
     * @return start index
     */
    public int getSidx() {
        return sidx;
    }

    /**
     * get end index
     * @return end index
     */
    public int getEidx() {
        return eidx;
    }

    /**
     * get line
     * @return line
     */
    public int getLine() {
        return line;
    }

    /**
     * get character position in line
     * @return character position in line
     */
    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    /**
     * check whether node is terminal
     * @return true if node is a terminal node
     */
    public boolean isTerminal() {
        return isLeaf() && ntype.isEmpty(); }

    /**
     * get label
     *
     * @return unescaped label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParseTreeNode))
            return false;

        ParseTreeNode n = (ParseTreeNode) o;
        return n.id == id && n.ntype.equals(ntype) &&
                n.label.equals(label) && children.equals(n.children);
    }

    @Override
    public String toString() {
        return id + " " + ntype + " " + label;
    }

    /**
     * check whether this node is a leaf node
     *
     * @return true if node has no children, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
}
