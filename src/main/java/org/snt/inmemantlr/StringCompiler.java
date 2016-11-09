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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.memobjects.MemoryByteCode;
import org.snt.inmemantlr.memobjects.MemorySource;
import org.snt.inmemantlr.memobjects.MemoryTuple;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.tool.StringCodeGenPipeline;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * a compiler for strings
 */
public class StringCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringCompiler.class);

    private SpecialClassLoader cl = null;
    private MemoryTupleSet mt = null;
    private Map<String, Lexer> lexer = null;
    private Map<String, Parser> parser = null;
    private Map<String, Class<?>> classes = new HashMap<>();

    /**
     * constructors
     */
    public StringCompiler() {
        cl = new SpecialClassLoader(getClass().getClassLoader());
        lexer = new HashMap<>();
        parser = new HashMap<>();
        mt = new MemoryTupleSet();
    }

    public void load(MemoryTupleSet mset) {
        assert mset != null && mset.size() > 0;
        mt.addAll(mset);
        for (MemoryTuple tup : mset) {
            for (MemoryByteCode bc : tup.getByteCodeObjects()) {
                cl.addClass(bc);
            }
        }
    }

    private static final Class[] parameters = new Class[]{URL.class};

    /**
     * do the compilation for the antlr artifacts
     *
     * @param scgps string code generation pipeline
     * @return true if compilation was successful, false otherwise
     */
    public boolean compile(Set<StringCodeGenPipeline> scgps) {
        JavaCompiler javac = new EclipseCompiler();

        StandardJavaFileManager sjfm = javac.getStandardFileManager(null, null, null);
        SpecialJavaFileManager fileManager = new SpecialJavaFileManager(sjfm, cl);

        List<MemorySource> cunit = new ArrayList();
        Set<MemorySource> mset = new HashSet();

        for(StringCodeGenPipeline sc : scgps) {
            cunit.addAll(getCompilationUnits(sc));
        }

        mset.addAll(cunit);

        DiagnosticListener<? super JavaFileObject> dianosticListener = null;
        Iterable<String> classes = null;

        Writer out = new PrintWriter(System.out);

        List<String> optionList = new ArrayList<>();

        JavaCompiler.CompilationTask compile = javac.getTask(out, fileManager,
                dianosticListener, optionList, classes, cunit);



        boolean ret = compile.call();

        for (MemorySource ms : mset) {
            LOGGER.debug("get {} from file manager ", ms.getClassName());
            Set<MemoryByteCode> mb = fileManager.getByteCodeFromClass(ms.getClassName());
            assert mb != null;
            // book keeping of source-bytecode tuples
            mt.addMemoryTuple(ms, mb);
        }

        return ret;
    }

    /**
     * do the compilation for the antlr artifacts
     *
     * @param scgp string code generation pipeline
     * @return true if compilation was successful, false otherwise
     */
    public boolean compile(StringCodeGenPipeline scgp) {
        return compile(Collections.singleton(scgp));
    }


    private List<MemorySource> getCompilationUnits(StringCodeGenPipeline
                                                           scgp) {


        List<MemorySource> compilationUnits = new ArrayList<>();

        if (scgp.hasLexer()) {
            MemorySource ms = new MemorySource(scgp.getLexerName(), scgp.getLexer().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }
        if (scgp.hasBaseListener()) {
            MemorySource ms = new MemorySource(scgp.getBaseListenerName(), scgp.getBaseListener().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }
        if (scgp.hasBaseVisitor()) {
            MemorySource ms = new MemorySource(scgp.getBaseVisitorName(), scgp.getBaseVisitor().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }
        if (scgp.hasParser()) {
            MemorySource ms = new MemorySource(scgp.getParserName(), scgp.getParser().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }
        if (scgp.hasListener()) {
            MemorySource ms = new MemorySource(scgp.getListenerName(), scgp.getListener().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }
        if (scgp.hasVisitor()) {
            MemorySource ms = new MemorySource(scgp.getBaseVisitorName(), scgp.getBaseVisitor().render());
            LOGGER.debug("add memory source {}", ms.getClassName());
            compilationUnits.add(ms);
        }

        return compilationUnits;
    }

    /**
     * find class based on class name
     *
     * @param cname class
     * @return a class
     */
    private Class<?> findClass(String cname) {
        Class clazz;
        try {
            if (classes.containsKey(cname)) {
                clazz = classes.get(cname);
            } else {
                clazz = cl.findClass(cname);
                classes.put(cname, clazz);
            }
        } catch (ClassNotFoundException e) {
            return null;
        }

        return clazz;
    }

    /**
     * instanciate new lexer
     *
     * @param input lexer class content as character stream
     * @param cname class name
     * @return antlr lexer
     */
    public Lexer instanciateLexer(CharStream input, String cname) {
        return instanciateLexer(input, cname, true);
    }

    /**
     * instanciate new lexer
     *
     * @param input lexer class content as character stream
     * @param lexerClassName class name
     * @param useCached true to used cached lexers, otherwise false
     * @return antlr lexer
     */
    public Lexer instanciateLexer(CharStream input, String lexerClassName, boolean
            useCached) {
        Lexer elexer;

        if (useCached && lexer.containsKey(lexerClassName)) {
            elexer = lexer.get(lexerClassName);
            elexer.reset();
            elexer.setInputStream(input);
            return elexer;
        }

        Class<?> elex = findClass(lexerClassName);
        if (elex == null)
            return null;

        Constructor<?>[] cstr = elex.getConstructors();
        assert cstr.length == 1;

        try {
            elexer = (Lexer) cstr[0].newInstance(input);
            lexer.put(lexerClassName, elexer);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return elexer;
    }

    /**
     * instanciate new parser
     *
     * @param tstream parser class content as character stream
     * @param parserClassName class name
     * @return antlr parser
     */
    public Parser instanciateParser(CommonTokenStream tstream, String parserClassName) {
        Parser eparser;
        Class<?> elex = findClass(parserClassName);
        assert elex != null;
        Constructor<?>[] cstr = elex.getConstructors();
        assert cstr.length == 1;

        try {
            eparser = (Parser) cstr[0].newInstance(tstream);
            parser.put(parserClassName, eparser);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return eparser;
    }

    /**
     * get all compiled antlr objects (lexer, parser, etc) in source and bytecode format
     *
     * @return memory tuple set
     */
    public MemoryTupleSet getAllCompiledObjects() {
        return mt;
    }
}
