package org.snt.inmemantlr;

import org.antlr.v4.Tool;

/**
 * implemented by classes that wish to customize the given Antrl Tool
 * before use: For example, to add an error listener
 */
public interface ToolCustomizer {

    void customize(Tool t);
}
