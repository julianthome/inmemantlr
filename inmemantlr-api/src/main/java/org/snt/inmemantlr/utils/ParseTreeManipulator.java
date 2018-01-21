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
                toinj.getEidx());


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
     * @throws InjectionException
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
                toinject.getRoot().getFirstChild().getEidx());

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
