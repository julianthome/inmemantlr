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

package org.snt.inmemantlr.utils;

import org.snt.inmemantlr.exceptions.InjectionException;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

public enum ParseTreeManipulator {

    INTANCE;

    private void expand(ParseTree orig, ParseTreeNode par,
                                ParseTreeNode toinj) {

        ParseTreeNode nn = orig.newNode(par,
                toinj.getRule(),
                toinj.getLabel(),
                toinj.getSidx(),
                toinj.getEidx(),
                toinj.getLine(),
                toinj.getCharPositionInLine());


        for (ParseTreeNode c : toinj.getChildren()) {
            expand(orig, nn, c);
        }

        par.addChild(nn);

    }


    /**
     * Inject a parse tree into another one
     * @param rcv the receiving parse tree
     * @param injectionPoint injection point that defines the node
     *                       after/before the tree should be injected
     * @param toinject the tree to inject
     * @throws InjectionException parse tree could not be injected
     */
    public void inject(ParseTree rcv,
                       InjectionPointDetector injectionPoint,
                       ParseTree toinject) throws InjectionException {

        ParseTreeNode ip = injectionPoint.detect(rcv);

        if (!ip.hasParent())
            throw new InjectionException("injection point without parent");

        ParseTreeNode par = ip.getParent();

        ParseTreeNode nn = rcv.newNode(par,
                toinject.getRoot().getFirstChild().getRule(),
                toinject.getRoot().getFirstChild().getLabel(),
                toinject.getRoot().getFirstChild().getSidx(),
                toinject.getRoot().getFirstChild().getEidx(),
                toinject.getRoot().getFirstChild().getLine(),
                toinject.getRoot().getFirstChild().getCharPositionInLine());

        //par.addChild(nn);

        int pos = par.getChildren().indexOf(ip);


        switch (injectionPoint.getPosition()) {
            case BEFORE:
                par.getChildren().add(pos, nn);
                break;
            case AFTER:
                par.getChildren().add(pos + 1, nn);
                break;
        }

        for(ParseTreeNode cc : toinject.getRoot().getFirstChild().getChildren
                ()) {
            expand(rcv, nn, cc);
        }


    }


}
