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

        sb.append("\tnode [fontname=Helvetica,fontsize=11];\n");
        sb.append("\tedge [fontname=Helvetica,fontsize=10];\n");

        for(AstNode rn : this.nodes) {
            sb.append("\tn" + rn.getId() + " [label=\"(" +rn.getId() +")\\n"  +
                    rn.getLabel() + "\\n" + rn.getType().toString() + "\"];\n");
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
