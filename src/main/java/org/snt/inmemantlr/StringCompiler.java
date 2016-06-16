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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringCompiler {

    private static SpecialClassLoader cl = new SpecialClassLoader();

    public static boolean compile(StringCodeGenPipeline scgp) {

        JavaCompiler javac = new EclipseCompiler();

        StandardJavaFileManager sjfm = javac.getStandardFileManager(null, null, null);
        SpecialJavaFileManager fileManager = new SpecialJavaFileManager(sjfm, cl);
        List<MemorySource> compilationUnits = new ArrayList<MemorySource>();

        if (scgp.hasLexer()) {
            compilationUnits.add(new MemorySource(scgp.getLexerName(), scgp.getLexer().render()));
        }
        if (scgp.hasBaseListener()) {
            compilationUnits.add(new MemorySource(scgp.getBaseListenerName(), scgp.getBaseListener().render()));
        }
        if (scgp.hasBaseVisitor()) {
            compilationUnits.add(new MemorySource(scgp.getBaseVisitorName(), scgp.getBaseVisitor().render()));
        }
        if (scgp.hasParser()) {
            compilationUnits.add(new MemorySource(scgp.getParserName(), scgp.getParser().render()));
        }
        if (scgp.hasListener()) {
            compilationUnits.add(new MemorySource(scgp.getListenerName(), scgp.getListener().render()));
        }
        if (scgp.hasVisitor()) {
            compilationUnits.add(new MemorySource(scgp.getBaseVisitorName(), scgp.getVisitor().render()));
        }


        DiagnosticListener<? super JavaFileObject> dianosticListener = null;
        Iterable<String> classes = null;
        Writer out = new PrintWriter(System.out);

        List<String> optionList = new ArrayList<String>();

        JavaCompiler.CompilationTask compile = javac.getTask(out, fileManager, dianosticListener, optionList, classes, compilationUnits);

        return compile.call();
    }

    private static Class<?> findClass(String cname) {
        try {
            return cl.findClass(cname);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    public static Lexer instanciateLexer(CharStream input, String cname) {

        String name = cname + "Lexer";

        Lexer ret = null;


        Class<?> elex = StringCompiler.findClass(name);

        Constructor<?>[] cstr = elex.getConstructors();

        assert (cstr.length == 1);

        try {
            //System.out.println(cstr[0].toGenericString());
            ret = (Lexer) cstr[0].newInstance(input);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return ret;
    }

    public static Parser instanciateParser(CommonTokenStream tstream, String cname) {

        String name = cname + "Parser";

        Parser ret = null;

        Class<?> elex = StringCompiler.findClass(name);

        Constructor<?>[] cstr = elex.getConstructors();

        assert (cstr.length == 1);

        try {
            ret = (Parser) cstr[0].newInstance(tstream);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return ret;
    }

}
