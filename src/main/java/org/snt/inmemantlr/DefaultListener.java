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

package org.snt.inmemantlr;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * default tree listener
 */
public class DefaultListener implements ParseTreeListener, Serializable {

    private static final long serialVersionUID = 7449676975470436260L;

    protected Parser parser;

    private final Map<String, Integer> rmap = new HashMap<>();

    /**
     * constructor
     */
    public DefaultListener() {
        parser = null;
    }

    /**
     * maps rule index to its actual name
     *
     * @param key rule index
     * @return the corresponding rule name
     */
    public String getRuleByKey(int key) {
        return rmap.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), key))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * set parser
     *
     * @param p parser
     */
    protected void setParser(Parser p) {
        parser = p;
        rmap.clear();
        rmap.putAll(parser.getRuleIndexMap());
    }

    public void reset() {
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
    }
}
