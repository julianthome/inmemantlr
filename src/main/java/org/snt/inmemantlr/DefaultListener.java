/*
* Inmemantlr - In memory compiler for Antlr 4
*
* Copyright 2007, Julian Thomé <julian.thome@uni.lu>
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

import java.util.*;


public class DefaultListener implements ParseTreeListener {

	protected Parser parser;
	private Map<String,Integer> rmap;
	private Stack<Comparator> compstack = new Stack<Comparator>();
	private Stack<String> context = new Stack<String>();

	private static DefaultListener listener = null;

	public DefaultListener() {
		this.parser = null;
		this.rmap = null;
	}

	public String getRuleByKey(int key) {

		for(Map.Entry<String, Integer> e : this.rmap.entrySet()) {
			if (e.getValue() == key)
				return e.getKey();
		}
		return null;
	}

	protected void setParser(Parser p) {
		this.parser = p;
		this.rmap = this.parser.getRuleIndexMap();
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
