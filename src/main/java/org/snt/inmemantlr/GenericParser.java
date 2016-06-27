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


import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.tool.Grammar;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class GenericParser {

	private Tool antlr;
	private String cname;
	private Grammar g = null;
	private boolean compiled;
	private boolean parsed;
	private StringCodeGenPipeline gen;
	private ParserRuleContext data;
	private DefaultListener listener;

	public GenericParser(String grammarFile, String name) {
		this.antlr = new Tool();
		this.cname = name;
		this.g = antlr.loadGrammar(grammarFile);
		this.gen = new StringCodeGenPipeline(g, cname);
		this.compiled = false;
		this.parsed = false;
		this.data = null;
	}

	public boolean compile() {
		gen.process();
		compiled = StringCompiler.compile(gen);
		return compiled;
	}


	public ParserRuleContext parse(String toParse) throws IllegalWorkflowException {
		if(listener == null) {
			throw new IllegalWorkflowException("Listener is not set");
		}
		if(!compiled) {
			throw new IllegalWorkflowException("Parser not yet compiled");
		}

		ANTLRInputStream input = new ANTLRInputStream(toParse);

		Lexer lex = StringCompiler.instanciateLexer(input, cname);

		CommonTokenStream tokens = new CommonTokenStream(lex);

		tokens.fill();

		Parser parser = StringCompiler.instanciateParser(tokens, cname);

		// make parser information available to listener
		this.listener.setParser(parser);

		parser.addErrorListener(new DiagnosticErrorListener());
		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		parser.setBuildParseTree(true);
		parser.setTokenStream(tokens);

		String [] rules = parser.getRuleNames();
		String EntryPoint = rules[0];


		try {
			Class<?> pc = parser.getClass();
			Method m = pc.getMethod(EntryPoint,(Class<?>[] ) null);
			data = (ParserRuleContext) m.invoke(parser, (Object []) null);
		} catch (NoSuchMethodException | SecurityException |
				IllegalAccessException | IllegalArgumentException |
				InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		this.parsed = true;

		System.out.println(data.toStringTree(parser));
		System.out.println(data.toInfoString(parser));

		ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(this.listener, data);

		return this.data;
	}

	public ParseTreeListener getListener() {
		return this.listener;
	}

	public void setListener(DefaultListener listener) {
		this.listener = listener;
	}

}
