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

import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.tree.Ast;

import java.io.File;
import java.io.FileNotFoundException;


public enum GraalParser {
    INSTANCE;


    private static File [] files = {
            GraalUtils.getResource("ANTLRv4Lexer.g4"),
            GraalUtils.getResource("ANTLRv4Parser.g4"),
            GraalUtils.getResource("LexBasic.g4")
    };


    private static GenericParser gp;
    private static DefaultTreeListener dt = new DefaultTreeListener();


    static {
        // Exam
        ToolCustomizer tc = t -> t.genPackage = "org.antlr.parser.antlr4";

        try {
            gp = new GenericParser(tc,files);
        } catch (FileNotFoundException e) {
            assert false;
        }

        gp.setListener(dt);

        try {
            File util =  GraalUtils.getResource("src/main/java/org/antlr/parser/antlr4/LexerAdaptor.java");
            gp.addUtilityJavaFiles(util);
        } catch (FileNotFoundException e) {
            assert false;
        }

        boolean compile;
        try {
            gp.compile();
        } catch (CompilationException e) {
            assert false;
        }
    }

    public Ast getAstForGrammar(File grammar) throws FileNotFoundException {
        try {
            gp.parse(grammar);
        } catch (IllegalWorkflowException e) {
            assert false;
        }
        return dt.getAst();
    }


}
