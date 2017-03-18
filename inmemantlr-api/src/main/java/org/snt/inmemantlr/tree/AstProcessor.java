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

import org.snt.inmemantlr.exceptions.AstProcessorException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * processor to process an abstract syntax tree
 *
 * @param <R> return type of result
 * @param <T> datatype to which an AST node can be mapped to
 */
public abstract class AstProcessor<R, T> {

    protected Ast ast = null;
    protected Map<AstNode, T> smap;
    protected LinkedList<AstNode> active;

    private Map<AstNode, Integer> nmap;

    /**
     * constructor
     *
     * @param ast abstract syntax tree to process
     */
    public AstProcessor(Ast ast) {
        this.ast = ast;
        nmap = new HashMap<>();
        smap = new HashMap<>();
        active = new LinkedList<>();
    }

    /**
     * process the abstract syntax tree
     *
     * @return result
     * @throws AstProcessorException if something went wrong while processing
     * an ast node
     */
    public R process() throws AstProcessorException {
        initialize();

        for (AstNode rn : ast.getNodes()) {
            nmap.put(rn, rn.getChildren().size());
        }

        active.addAll(ast.getLeafs());

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
     *
     * @return debugging string
     */
    public String debug() {
        StringBuilder sb = new StringBuilder();

        sb.append(".....Smap......\n");
        for (Map.Entry<AstNode, T> e : smap.entrySet()) {
            sb.append(e.getKey().getId()).append(" :: ").append(e.getValue()).append("\n");
        }

        sb.append(".....Nmap......\n");

        for (Map.Entry<AstNode, Integer> e : nmap.entrySet()) {
            sb.append(e.getKey().getId()).append(" :: ").append(e.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * helper function
     *
     * @param n ast node
     */
    public void simpleProp(AstNode n) {
        if (n.getChildren().size() == 1) {
            smap.put(n, smap.get(n.getFirstChild()));
        }
    }

    /**
     * helper function
     *
     * @param n ast node
     * @return data mapped to n
     */
    public T getElement(AstNode n) {
        if (!smap.containsKey(n))
            throw new IllegalArgumentException("smap must contain AstNode");

        return smap.get(n);
    }

    /**
     * get processing result
     *
     * @return result
     */
    public abstract R getResult();

    /**
     * initialization function
     */
    protected abstract void initialize();

    /**
     * process a single ast node
     *
     * @param n an ast node to process
     * @throws AstProcessorException if something went wrong while processing
     * an ast node
     */
    protected abstract void process(AstNode n) throws AstProcessorException;
}
