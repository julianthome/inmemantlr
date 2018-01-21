package org.snt.inmemantlr.utils;

import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

public interface InjectionPointDetector {

    enum Position {
        BEFORE,
        AFTER
    }

    ParseTreeNode detect(ParseTree t);
    Position getPosition();
}
