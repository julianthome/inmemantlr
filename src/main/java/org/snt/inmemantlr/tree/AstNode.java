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
