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

import java.util.*;


public abstract class AstProcessor<R, T> {

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
            active.add(l);
        }

        while (!active.isEmpty()) {
            AstNode rn = active.poll();

            process(rn);

            AstNode parent = rn.getParent();

            if (parent != null) {
                nmap.replace(parent, nmap.get(parent) - 1);
                ;
                if (nmap.get(parent) == 0) {
                    active.add(parent);
                }
            }

        }

        return getResult();
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

    public void simpleProp(AstNode n) {
        if (n.getChildren().size() == 1) {
            this.smap.put(n, this.smap.get(n.getFirstChild()));
        }
    }

    public T getElement(AstNode n) {
        assert (this.smap.containsKey(n));
        return this.smap.get(n);
    }

    public abstract R getResult();

    protected abstract void initialize();

    protected abstract void process(AstNode n);

}
