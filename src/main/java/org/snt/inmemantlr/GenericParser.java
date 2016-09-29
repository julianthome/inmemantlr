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

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.DeserializationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.SerializationException;
import org.snt.inmemantlr.memobjects.GenericParserSerialize;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * generic parser - an antlr parser representation
 */
public class GenericParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParser.class);

    private Tool antlr;
    private String cname;
    private Grammar g = null;
    private StringCodeGenPipeline gen;
    private DefaultListener listener;
    private StringCompiler sc;
    private File gfile;
    private String gconent;
    private boolean useCached = true;

    /**
     * constructor
     *
     * @param grammarFile path to grammar file
     * @param name parser name
     */
    public GenericParser(File grammarFile, String name) {
        this(grammarFile, name, null);
    }

    /**
     * constructor
     *
     * @param content grammar file content
     * @param name parser name
     */
    public GenericParser(String content, String name) {
        this(content, name, null);
    }

    /**
     * constructor
     *
     * @param grammarFile grammar file
     * @param name grammar name
     * @param tlc a ToolCustomizer
     */
    public GenericParser(File grammarFile, String name, ToolCustomizer tlc) {
        this(FileUtils.loadFileContent(grammarFile.getAbsolutePath()), name, tlc);
        gfile = grammarFile;
    }

    /**
     * constructor
     *
     * @param content grammar file content
     * @param name grammar
     * @param tlc a ToolCustomizer
     */
    public GenericParser(String content, String name, ToolCustomizer tlc) {
        antlr = new Tool();
        if (tlc != null) {
            tlc.customize(antlr);
        }
        cname = name;
        gconent = content;
        g = loadGrammarFromString(content, name);
        gen = new StringCodeGenPipeline(g, cname);
        sc = new StringCompiler();
    }

    /**
     * constructor
     *
     * @param content grammar file content
     * @param name grammar
     * @param tlc a ToolCustomizer
     * @param useCached true to used cached lexers, otherwise false
     */
    public GenericParser(String content, String name, ToolCustomizer tlc, boolean useCached) {
        this(content, name, tlc);
        this.useCached = useCached;
    }

    public static GenericParser independentInstance(String content, String name, ToolCustomizer tlc) {
        return new GenericParser(content, name, tlc, false);
    }

    /**
     * compile grammar file
     *
     * @return true if compilation was successful, false otherwise
     */
    public boolean compile() {
        // the antlr objects are already compiled
        if (antrlObjectsAvailable())
            return false;

        LOGGER.debug("compiled");
        gen.process();
        return sc.compile(gen);
    }

    /**
     * load antlr grammar from string
     *
     * @param content string content from antlr grammar
     * @param name name of antlr grammar
     * @return grammar object
     */
    public Grammar loadGrammarFromString(String content, String name) {
        GrammarRootAST grammarRootAST = antlr.parseGrammarFromString(content);
        final Grammar g = antlr.createGrammar(grammarRootAST);
        g.fileName = name;
        antlr.process(g, false);
        return g;
    }

    /**
     * parse string an create a context
     *
     * @param toParse string to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse) throws IllegalWorkflowException {
        DefaultListener listener = this.listener == null ? new DefaultListener() : this.listener;
        listener.reset();
        return parse(toParse, listener, null);
    }

    /**
     * parse in fresh
     *
     * @param toParse string to parse
     * @param listener a ParseTreeListener
     * @param production Production name to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse, DefaultListener listener, String production) throws IllegalWorkflowException {
        if (listener != null) {
            this.listener = listener;
        }
        if (!antrlObjectsAvailable()) {
            throw new IllegalWorkflowException("No antlr objects have been compiled or loaded");
        }

        ANTLRInputStream input = new ANTLRInputStream(toParse);

        Lexer lex = sc.instanciateLexer(input, cname, useCached);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        tokens.fill();

        Parser parser = sc.instanciateParser(tokens, cname);

        // make parser information available to listener
        this.listener.setParser(parser);

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
     * get antlr grammar object
     *
     * @return antlr grammar object
     */
    public Grammar getGrammar() {
        return g;
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

        GenericParserSerialize towrite = new GenericParserSerialize(gfile,
                gconent,
                getAllCompiledObjects(),
                cname);

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

        GenericParser gp;

        if (gin.getGrammarFile() != null && gin.getGrammarFile().exists()) {
            gp = new GenericParser(gin.getGrammarFile(), gin.getCname());
        } else if (gin.getGrammarContent() != null && !gin.getGrammarContent().isEmpty()) {
            gp = new GenericParser(gin.getGrammarContent(), gin.getCname(), null);
        } else {
            throw new DeserializationException("cannot deserialize " + file);
        }

        gp.sc.load(gin.getMemoryTupleSet());

        if (!gp.antrlObjectsAvailable()) {
            throw new DeserializationException("there are no antlr objects available in " + file);
        }

        return gp;
    }
}
