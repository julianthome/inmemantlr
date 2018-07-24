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

import org.snt.inmemantlr.exceptions.ParseTreeProcessorException;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * processor for processing a parse tree
 *
 * @param <R> return type of result
 * @param <T> datatype to which an AST node can be mapped to
 */
public abstract class ParseTreeProcessor<R, T> {

    protected ParseTree parseTree = null;
    protected Map<ParseTreeNode, T> smap;
    protected Queue<ParseTreeNode> active;

    private Map<ParseTreeNode, Integer> nmap;

    /**
     * constructor
     *
     * @param parseTree abstract syntax tree to process
     */
    public ParseTreeProcessor(ParseTree parseTree) {
        this.parseTree = parseTree;
        nmap = new HashMap<>();
        smap = new HashMap<>();
        active = new ArrayDeque<>();
    }

    /**
     * process the abstract syntax tree
     *
     * @return result
     * @throws ParseTreeProcessorException if something went wrong while processing
     * an ast node
     */
    public R process() throws ParseTreeProcessorException {

        // sort nodes topologically first
        this.parseTree.topoSort();

        initialize();

        for (ParseTreeNode rn : parseTree.getNodes()) {
            nmap.put(rn, rn.getChildren().size());
        }

        active.addAll(parseTree.getLeafs());

        while (!active.isEmpty()) {
            ParseTreeNode rn = active.poll();

            process(rn);

            ParseTreeNode parent = rn.getParent();

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
        for (Map.Entry<ParseTreeNode, T> e : smap.entrySet()) {
            sb.append(e.getKey().getId()).append(" :: ").append(e.getValue()).append("\n");
        }

        sb.append(".....Nmap......\n");

        for (Map.Entry<ParseTreeNode, Integer> e : nmap.entrySet()) {
            sb.append(e.getKey().getId()).append(" :: ").append(e.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * helper function
     *
     * @param n ast node
     */
    public void simpleProp(ParseTreeNode n) {
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
    public T getElement(ParseTreeNode n) {
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
     * @throws ParseTreeProcessorException if something went wrong while processing
     * an ast node
     */
    protected abstract void process(ParseTreeNode n) throws ParseTreeProcessorException;
}
