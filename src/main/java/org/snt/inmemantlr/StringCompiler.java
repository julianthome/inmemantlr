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

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * a compiler for strings
 */
public class StringCompiler {

    final static Logger logger = LoggerFactory.getLogger(StringCompiler.class);

    private SpecialClassLoader cl = null;

    private MemoryTupleSet mt = null;
    private Map<String, Lexer> lexer = null;
    private Map<String, Parser> parser = null;


    /**
     * constructors
     */
    public StringCompiler() {
        this.cl = new SpecialClassLoader(getClass().getClassLoader());
        this.lexer = new HashMap<>();
        this.parser = new HashMap<>();
        this.mt = new MemoryTupleSet();
    }

    public void load(MemoryTupleSet mset) {
        assert(mset != null && mset.size() > 0);
        this.mt.addAll(mset);
        for(MemoryTuple tup : mset) {
            for(MemoryByteCode bc : tup.getByteCodeObjects()) {
                this.cl.addClass(bc);
            }
        }
    }

    /**
     * do the compilation for the antlr artifacts
     * @param scgp string code generation pipeline
     * @return true if compilation was successful, false otherwise
     */
    public boolean compile(StringCodeGenPipeline scgp) {

        JavaCompiler javac = new EclipseCompiler();

        StandardJavaFileManager sjfm = javac.getStandardFileManager(null, null, null);
        SpecialJavaFileManager fileManager = new SpecialJavaFileManager(sjfm, cl);

        List<MemorySource> compilationUnits = new ArrayList<MemorySource>();


        Set<MemorySource> mset = new HashSet<>();

        if (scgp.hasLexer()) {
            MemorySource ms = new MemorySource(scgp.getLexerName(), scgp.getLexer().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }
        if (scgp.hasBaseListener()) {
            MemorySource ms = new MemorySource(scgp.getBaseListenerName(), scgp.getBaseListener().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }
        if (scgp.hasBaseVisitor()) {
            MemorySource ms = new MemorySource(scgp.getBaseVisitorName(), scgp.getBaseVisitor().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }
        if (scgp.hasParser()) {
            MemorySource ms = new MemorySource(scgp.getParserName(), scgp.getParser().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }
        if (scgp.hasListener()) {
            MemorySource ms = new MemorySource(scgp.getListenerName(), scgp.getListener().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }
        if (scgp.hasVisitor()) {
            MemorySource ms = new MemorySource(scgp.getBaseVisitorName(), scgp.getBaseVisitor().render());
            logger.debug("add memory source " + ms.getClassName());
            compilationUnits.add(ms);
            mset.add(ms);
        }

        DiagnosticListener<? super JavaFileObject> dianosticListener = null;
        Iterable<String> classes = null;
        Writer out = new PrintWriter(System.out);

        List<String> optionList = new ArrayList<String>();

        JavaCompiler.CompilationTask compile = javac.getTask(out, fileManager, dianosticListener, optionList, classes, compilationUnits);

        boolean ret = compile.call();

        for(MemorySource ms : mset) {
            Set<MemoryByteCode> mb = fileManager.getByteCodeFromClass(ms.getClassName());
            assert (mb != null);
            // book keeping of source-bytecode tuples
            this.mt.addMemoryTuple(ms, mb);
        }

        return ret;
    }

    /**
     * find classs based on class name
     * @param cname class
     * @return
     */
    private Class<?> findClass(String cname) {
        try {
            return this.cl.findClass(cname);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }


    /**
     * instanciate new lexer
     * @param input lexer class content as character stream
     * @param cname class name
     * @return antlr lexer
     */
    public Lexer instanciateLexer(CharStream input, String cname) {

        Lexer elexer = null;

        String name = cname + "Lexer";

        if (lexer.containsKey(name)) {
            elexer = lexer.get(name);
            elexer.reset();
            return elexer;
        }

        Lexer ret = null;


        Class<?> elex = findClass(name);

        if(elex == null)
            return null;

        Constructor<?>[] cstr = elex.getConstructors();

        assert (cstr.length == 1);

        try {
            //System.out.println(cstr[0].toGenericString());
            elexer = (Lexer) cstr[0].newInstance(input);
            lexer.put(name, elexer);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return elexer;
    }

    /**
     * instanciate new parser
     * @param tstream parser class content as character stream
     * @param cname class name
     * @return antlr parser
     */
    public Parser instanciateParser(CommonTokenStream tstream, String cname) {

        String name = cname + "Parser";

        Parser eparser = null;

        if (parser.containsKey(name)) {
            eparser = parser.get(name);
            eparser.reset();
            return eparser;
        }

        Parser ret = null;

        Class<?> elex = findClass(name);

        Constructor<?>[] cstr = elex.getConstructors();

        assert (cstr.length == 1);

        try {
            eparser = (Parser) cstr[0].newInstance(tstream);
            parser.put(name, eparser);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }

        return eparser;
    }

    /**
     * get all compiled antlr objects (lexer, parser, etc) in source and
     * bytecode format
     * @return memory tuple set
     */
    public MemoryTupleSet getAllCompiledObjects() {
        return this.mt;
    }




}
