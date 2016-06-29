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

import org.snt.inmemantlr.utils.EscapeUtils;

import java.util.List;
import java.util.Vector;

public class AstNode {

    private String label;
    private String ntype;
    private AstNode parent;
    private Ast tree;
    private int id;

    private List<AstNode> children;


    private AstNode(Ast tree) {
        this.tree = tree;
        this.id = ++this.tree.cnt;
        this.children = new Vector<AstNode>();
    }

    protected AstNode(Ast tree, AstNode parent, String nt, String label) {
        this(tree);
        this.ntype = nt;
        this.label = label;
        this.parent = parent;
    }

    /**
     * Deep copy constructor
     * @param tree
     * @param nod
     */
    protected AstNode(Ast tree, AstNode nod) {
        this(tree);
        this.id = nod.id;
        this.ntype = nod.ntype;
        this.label = nod.label;
        for(AstNode c : nod.children) {
            AstNode cnod = new AstNode(tree,c);
            cnod.parent = this;
            this.tree.nodes.add(cnod);
            this.children.add(cnod);
        }
    }

    public AstNode getLastChild(){
        if(this.children.size() > 0) {
            return this.children.get(this.children.size()-1);
        }
        return null;
    }

    public AstNode getFirstChild(){
        if(!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    public boolean hasParent() {
        return this.parent != null;
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

    public void delChild(AstNode n) {
        this.children.remove(n);
    }

    public void replaceChild(AstNode oldNode, AstNode newNode) {
        if(this.children.contains(oldNode)) {
            this.children.set(this.children.indexOf(oldNode), newNode);
            newNode.parent = this;
        }
    }

    public int getId() {
        return this.id;
    }

    public String getRule() {
        return this.ntype;
    }

    public String getEscapedLabel() {
        return EscapeUtils.escapeSpecialCharacters(this.label);
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id + " " + this.ntype.toString() + " " + this.label;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AstNode))
            return false;

        AstNode n = (AstNode)o;

        return n.ntype.equals(this.ntype) &&
                n.label.equals(this.label) && this.children.equals(n.children);

    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

}
