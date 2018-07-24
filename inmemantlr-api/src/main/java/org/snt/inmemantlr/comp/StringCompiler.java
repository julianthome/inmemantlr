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

package org.snt.inmemantlr.comp;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.CompilationErrorException;
import org.snt.inmemantlr.memobjects.MemoryByteCode;
import org.snt.inmemantlr.memobjects.MemorySource;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;

import javax.tools.*;
import java.io.StringWriter;
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
    private static final String CONSTRUCTOR_ARG = "interface org.antlr" +
            ".v4.runtime.TokenStream";

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
        if (mset == null || mset.size() == 0)
            throw new IllegalArgumentException("mset must not be null or empty");

        mt.addAll(mset);
        mset.forEach(tup -> tup.getByteCodeObjects().forEach(bc -> cl.addClass(bc)));
    }

    private static final Class<?>[] parameters = new Class[]{URL.class};

    /**
     * do the compilation for the antlr artifacts
     * @param units string code generation pipeline
     * @param oprov compiler option provider
     * @throws CompilationErrorException if the compilation was not successful
     */
    public void compile(Set<CunitProvider> units, CompilerOptionsProvider oprov)
            throws
            CompilationErrorException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager sjfm = javac.getStandardFileManager(null, null, null);
        SpecialJavaFileManager fileManager = new SpecialJavaFileManager(sjfm, cl);

        List<MemorySource> cunit = new ArrayList<>();
        Set<MemorySource> mset = new HashSet<>();

        for (CunitProvider sc : units) {
            cunit.addAll(sc.getItems());
            for (MemorySource ms : sc.getItems()) {
                LOGGER.debug(ms.toString());
            }
        }

        mset.addAll(cunit);

        DiagnosticListener<? super JavaFileObject> dlistener = null;
        Iterable<String> classes = null;

        Writer out = new StringWriter();

        List<String> optionList = new ArrayList<>();

        optionList.addAll(oprov.getOptions());

        JavaCompiler.CompilationTask compile = javac.getTask(out, fileManager,
                dlistener, optionList, classes, cunit);

        boolean failedCompilation = !compile.call();

        if (failedCompilation) {
            throw new CompilationErrorException(out.toString());
        }

        // note that for the memory-source -- we just store the class name
        // the corresponding byte code
        for (MemorySource ms : mset) {
            Set<MemoryByteCode> mb = fileManager.getByteCodeFromClass(ms.getClassName());
            if (mb.isEmpty())
                throw new IllegalArgumentException("MemoryByteCode must not be empty");

            // book keeping of source-bytecode tuples
            mt.addMemoryTuple(ms, mb);
        }
    }

    /**
     * find class based on class name
     *
     * @param cname class
     * @return a class
     */
    private Class<?> findClass(String cname) {
        Class<?> clazz;
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
     * @param input          lexer class content as character stream
     * @param lexerClassName class name
     * @param useCached      true to used cached lexers, otherwise false
     * @return antlr lexer
     */
    public Lexer instanciateLexer(CharStream input, String lexerClassName, boolean useCached) {
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
        if (cstr.length != 1)
            throw new IllegalArgumentException("There must be only constructor");

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
     * @param tstream         parser class content as character stream
     * @param parserClassName class name
     * @return antlr parser
     */
    public Parser instanciateParser(CommonTokenStream tstream, String parserClassName) {
        Parser eparser;
        Class<?> elex = findClass(parserClassName);
        Objects.requireNonNull(elex, "Failed to find class " + parserClassName);
        Constructor<?>[] cstr = elex.getConstructors();
        if (cstr.length == 0)
            throw new IllegalArgumentException("Constructors must not be empty");

        int cidx = 0;

        for (Constructor<?> c : cstr) {
            LOGGER.debug(c.getParameters()[0].getType().toString());
            if (c.getParameterCount() == 1 && c.getParameters()[0].getType()
                    .toString().equals(CONSTRUCTOR_ARG)) {
                break;
            }
            cidx++;
        }

        try {
            eparser = (Parser) cstr[cidx].newInstance(tstream);
            parser.put(parserClassName, eparser);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error(e.getMessage());
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
