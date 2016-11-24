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

package org.snt.inmemantlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.comp.CunitProvider;
import org.snt.inmemantlr.comp.FileProvider;
import org.snt.inmemantlr.comp.StringCodeGenPipeline;
import org.snt.inmemantlr.comp.StringCompiler;
import org.snt.inmemantlr.exceptions.DeserializationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.SerializationException;
import org.snt.inmemantlr.listener.DefaultListener;
import org.snt.inmemantlr.memobjects.GenericParserSerialize;
import org.snt.inmemantlr.memobjects.MemorySource;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.tool.InmemantlrTool;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.utils.FileUtils;
import org.snt.inmemantlr.utils.Tuple;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * generic parser - an antlr parser representation
 */
public class GenericParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParser.class);

    private InmemantlrTool antlr = new InmemantlrTool();
    private DefaultListener listener = new DefaultListener();
    private StringCompiler sc = new StringCompiler();
    private FileProvider fp = new FileProvider();
    private boolean useCached = true;
    private String lexerName = "";
    private String parserName = "";


    private void init(Set<String> gcontent, ToolCustomizer tlc) {
        if (tlc != null) {
            tlc.customize(antlr);
        }
        Set<GrammarRootAST> ast = antlr.sortGrammarByTokenVocab(gcontent);
        for (GrammarRootAST gast : ast) {
            LOGGER.debug("gast {}", gast.getGrammarName());
            antlr.createPipeline(gast);
        }        
    }

    private GenericParser(MemoryTupleSet mset, String parserName, String
            lexerName){
        assert mset != null && mset.size() > 0;
        sc.load(mset);
        LOGGER.debug("parser ", parserName);
        LOGGER.debug("parser ", lexerName);
        this.parserName = parserName;
        this.lexerName = lexerName;
    }


    public void addUtilityJavaFile(File f) throws
            FileNotFoundException {

        String name = FilenameUtils.removeExtension(f
                        .getName());
        String content = FileUtils.loadFileContent(f);

        if(!f.exists()) {
            throw new FileNotFoundException("File " + f.getName() + " does " +
                    "not exist");
        }

        LOGGER.debug("add utiltiy {}", name);
        fp.addFiles(new MemorySource(name, content));
    }


    /**
     * constructor
     *
     * @param gcontent List of antlr grammar content
     * @param tlc a ToolCustomizer
     */
    public GenericParser(ToolCustomizer tlc, String ... gcontent) {
        init(new HashSet(Arrays.asList(gcontent)), tlc);
    }

    /**
     * constructor
     *
     * @param gcontent List of antlr grammar content
     */
    public GenericParser(String ... gcontent) {
        init(new HashSet(Arrays.asList(gcontent)), null);
    }



    /**
     * constructor
     *
     * @param gfile List of antlr grammar files
     * @param tlc a ToolCustomizer
     */
    public GenericParser(ToolCustomizer tlc, File ... gfile) throws FileNotFoundException {
        Set<String> gcontent = new HashSet();
        for(File f : gfile) {
            if(!f.exists() || !f.canRead())
                throw new FileNotFoundException("file " + f.getAbsolutePath()
                        + " does not exist or is not readable");

            gcontent.add(FileUtils.loadFileContent(f.getAbsolutePath()));
        }
        init(gcontent,tlc);
    }

    /**
     * constructor
     *
     * @param gfile List of antlr grammar files
     */
    public GenericParser(File ... gfile) throws FileNotFoundException {
        Set<String> gcontent = new HashSet();
        for(File f : gfile) {

            if(!f.exists() || !f.canRead())
                throw new FileNotFoundException("file " + f.getAbsolutePath()
                        + " does not exist or is not readable");

            gcontent.add(FileUtils.loadFileContent(f.getAbsolutePath()));
        }
        init(gcontent,null);
    }



    /**
     * constructor
     *
     * @param content grammar file content
     * @param tlc a ToolCustomizer
     * @param useCached true to used cached lexers, otherwise false
     */
    public GenericParser(ToolCustomizer tlc, boolean useCached, String ...
            content) {
        this(tlc,content);
        this.useCached = useCached;
    }

    /**
     * a signle instance of a generic parser
     *
     * @param content grammar content
     * @param tlc a ToolCustomizer
     * @return grammar object
     */
    public static GenericParser instance(ToolCustomizer tlc, String content) {
        return new GenericParser(tlc, content);
    }

    public static GenericParser independentInstance(ToolCustomizer tlc,
                                                    String content) {
        return new GenericParser(tlc, false, content);
    }

    public void setClassPath(List cp) {
        this.sc.setClassPath(cp);
    }


    public boolean compile() {
        LOGGER.debug("compile");
        // the antlr objects are already compiled
        if (antrlObjectsAvailable())
            return false;

        Set<StringCodeGenPipeline> pip = antlr.getPipelines();


        for(StringCodeGenPipeline p : pip) {
            for(MemorySource ms : p.getItems()) {
                LOGGER.debug(ms.getName() + " " + ms.toString());
            }
        }

        // process all grammar objects
        Tuple<String,String> parserLexer = antlr.process();

        parserName = parserLexer.getFirst();
        lexerName = parserLexer.getSecond();

        assert !lexerName.isEmpty();
        assert !parserName.isEmpty();

        Set<CunitProvider> cu = new LinkedHashSet();

        if(fp.hasItems()) {
            cu.add(fp);
        }

        cu.addAll(antlr.getCompilationUnits());


        if(!sc.compile(cu)) {
            return false;
        }

        return true;
    }

    /**
     * parse file content an create a context
     *
     * @param toParse string to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws FileNotFoundException if input file cannot be found
     */
    public ParserRuleContext parse(File toParse) throws
            IllegalWorkflowException, FileNotFoundException {
        return parse(toParse, null);
    }



    /**
     * parse string an create a context
     *
     * @param toParse string to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse) throws IllegalWorkflowException {
        return parse(toParse, null);
    }


    /**
     * parse in fresh
     *
     * @param toParse string to parse
     * @param production Production name to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(File toParse, String production) throws
            IllegalWorkflowException, FileNotFoundException {
        if(!toParse.exists()) {
            throw new FileNotFoundException("could not find file " + toParse
                    .getAbsolutePath());
        }
        return parse(FileUtils.loadFileContent(toParse.getAbsolutePath()), production);
    }

    /**
     * parse in fresh
     *
     * @param toParse string to parse
     * @param production Production name to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse, String production) throws IllegalWorkflowException {

        if (!antrlObjectsAvailable()) {
            throw new IllegalWorkflowException("No antlr objects have been compiled or loaded");
        }

        listener.reset();

        ANTLRInputStream input = new ANTLRInputStream(toParse);

        LOGGER.debug("load lexer {}", lexerName);
        Lexer lex = sc.instanciateLexer(input, lexerName, useCached);

        assert lex != null;

        CommonTokenStream tokens = new CommonTokenStream(lex);

        tokens.fill();

        LOGGER.debug("load parser {}", parserName);
        Parser parser = sc.instanciateParser(tokens, parserName);

        assert parser != null;

        // make parser information available to listener
        listener.setParser(parser);

        // parser.addErrorListener(new DiagnosticErrorListener());
        parser.removeErrorListeners();
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.setBuildParseTree(true);
        parser.setTokenStream(tokens);

        String[] rules = parser.getRuleNames();
        String entryPoint;
        if (production == null) {
            entryPoint = rules[0];
        } else {
            if (!Arrays.asList(rules).contains(production)) {
                throw new IllegalArgumentException("Rule " + production + " not found");
            }
            entryPoint = production;
        }

        ParserRuleContext data;
        try {
            Class<?> pc = parser.getClass();
            Method m = pc.getMethod(entryPoint, (Class<?>[]) null);
            data = (ParserRuleContext) m.invoke(parser, (Object[]) null);
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            return null;
        }

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, data);
        return data;
    }

    /**
     * get parse tree listener
     *
     * @return parse tree listener
     */
    public ParseTreeListener getListener() {
        return listener;
    }

    /**
     * set parse tree listener
     *
     * @param listener listener to use
     */
    public void setListener(DefaultListener listener) {
        this.listener = listener;
    }

    /**
     * get all compiled antlr objects (lexer, parser, etc) in source and bytecode format
     *
     * @return memory tuple set
     */
    public MemoryTupleSet getAllCompiledObjects() {
        return sc.getAllCompiledObjects();
    }

    public boolean antrlObjectsAvailable() {
        return getAllCompiledObjects().size() > 0;
    }


    public void store(String file, boolean overwrite) throws SerializationException {
        File loc = new File(file);
        File path = loc.getParentFile();

        LOGGER.debug("store file {}", loc.getAbsolutePath());

        if (loc.exists() && !overwrite) {
            throw new SerializationException("File " + file + " already exists");
        }
        if (!path.exists()) {
            throw new SerializationException("Cannot find path " + path.getAbsolutePath());
        }
        if (!antrlObjectsAvailable()) {
            throw new SerializationException("You have not compiled your grammar yet - there are no antlr objects available");
        }

        FileOutputStream f_out;
        ObjectOutputStream o_out;

        try {
            f_out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new SerializationException("output file cannot be found");
        }

        try {
            o_out = new ObjectOutputStream(f_out);
        } catch (IOException e) {
            throw new SerializationException("object output stream cannot be created");
        }

        GenericParserSerialize towrite = new GenericParserSerialize
                (getAllCompiledObjects(), parserName, lexerName);

        try {
            o_out.writeObject(towrite);
        } catch (NotSerializableException e) {
            LOGGER.error("Not serializable {}", e.getMessage());
        } catch (IOException e) {
            throw new SerializationException("error occurred while writing object", e);
        } finally {
            closeQuietly(o_out);
            closeQuietly(f_out);
        }
    }

    public static GenericParser load(String file) throws DeserializationException {
        File loc = new File(file);
        File path = loc.getParentFile();

        if (!loc.exists()) {
            throw new DeserializationException("File " + file + " does not exist");
        }
        if (!path.exists()) {
            throw new DeserializationException("Cannot find path " + path.getAbsolutePath());
        }

        LOGGER.debug("load file {}", loc.getAbsolutePath());

        FileInputStream f_in;
        ObjectInputStream o_in;

        try {
            f_in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DeserializationException("input file " + file + " cannot be found");
        }

        try {
            o_in = new ObjectInputStream(f_in);
        } catch (IOException e) {
            throw new DeserializationException("object input stream cannot be found", e);
        }

        Object toread;

        try {
            toread = o_in.readObject();
        } catch (NotSerializableException e) {
            throw new DeserializationException("cannot read object", e);
        } catch (ClassNotFoundException e) {
            throw new DeserializationException("cannot find class", e);
        } catch (IOException e) {
            throw new DeserializationException(e.getMessage(), e);
        } finally {
            closeQuietly(o_in);
            closeQuietly(f_in);
        }

        assert toread instanceof GenericParserSerialize;
        GenericParserSerialize gin = (GenericParserSerialize) toread;

        GenericParser gp = new GenericParser(gin.getMemoryTupleSet(), gin
                .getParserName(), gin.getLexerName());

        if (!gp.antrlObjectsAvailable()) {
            throw new DeserializationException("there are no antlr objects available in " + file);
        }

        return gp;
    }
}
