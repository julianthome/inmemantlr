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

/**
 * Ast processor to process an abstract syntax tree
 * @param <R> return type of result
 * @param <T> datatype to which an AST node can be mapped to
 */
public abstract class AstProcessor<R, T> {

    protected Ast ast = null;

    private Map<AstNode, Integer> nmap;
    protected Map<AstNode, T> smap;
    protected LinkedList<AstNode> active;

    /**
     * constructor
     * @param ast abstract syntax tree to process
     */
    public AstProcessor(Ast ast) {
        this.ast = ast;
        this.nmap = new HashMap<AstNode, Integer>();
        this.smap = new HashMap<AstNode, T>();
        this.active = new LinkedList<AstNode>();
    }

    /**
     * process the abstract syntax tree
     * @return result
     */
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
                if (nmap.get(parent) == 0) {
                    active.add(parent);
                }
            }

        }

        return getResult();
    }

    /**
     * helper to print debugging information
     * @return debugging string
     */
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

    /**
     * helper function
     * @param n ast node
     */
    public void simpleProp(AstNode n) {
        if (n.getChildren().size() == 1) {
            this.smap.put(n, this.smap.get(n.getFirstChild()));
        }
    }

    /**
     * helper function
     * @param n ast node
     * @return data mapped to n
     */
    public T getElement(AstNode n) {
        assert (this.smap.containsKey(n));
        return this.smap.get(n);
    }

    /**
     * get processing result
     * @return result
     */
    public abstract R getResult();

    /**
     * initialization function
     */
    protected abstract void initialize();

    /**
     * process a single ast node
     * @param n ast node
     */
    protected abstract void process(AstNode n);

}
