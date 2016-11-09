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
import org.snt.inmemantlr.grammar.InmemantlrGrammar;
import org.snt.inmemantlr.grammar.InmemantlrLexerGrammar;
import org.snt.inmemantlr.memobjects.GenericParserSerialize;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.tool.InmemantlrTool;
import org.snt.inmemantlr.tool.StringCodeGenPipeline;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * generic parser - an antlr parser representation
 */
public class GenericParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParser.class);

    private InmemantlrTool antlr = new InmemantlrTool();
    private Set<StringCodeGenPipeline> gen = new LinkedHashSet<>();
    private DefaultListener listener = new DefaultListener();
    private StringCompiler sc = new StringCompiler();
    private boolean useCached = true;
    private String lexerName = "";
    private String parserName = "";


    private void init(Set<String> gcontent, ToolCustomizer tlc) {
        if (tlc != null) {
            tlc.customize(antlr);
        }
        Set<GrammarRootAST> ast = antlr.sortGrammarByTokenVocab(gcontent);
        for (GrammarRootAST gast : ast) {
            StringCodeGenPipeline pip = getPipeline(gast);
            gen.add(pip);
        }
    }

    private GenericParser(MemoryTupleSet mset, String parserName, String
            lexerName){
        assert mset != null && mset.size() > 0;
        sc.load(mset);
        this.parserName = parserName;
        this.lexerName = lexerName;
    }

    /**
     * create an antlr grammar based on a string and the grammar name
     *
     * @param ast grammar ast
     * @return codegen pipeline object
     */
    private StringCodeGenPipeline getPipeline(GrammarRootAST ast) {
        final Grammar g = antlr.createGrammar(ast);
        if(g.isParser()) {
            parserName = g.name;
        } else if (g.isLexer()) {
            lexerName = g.name;
        } else {
            parserName = g.name + "Parser";
            lexerName = g.name + "Lexer";
        }
        g.fileName = g.name;
        return new StringCodeGenPipeline(g, g.name);
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
    public GenericParser(ToolCustomizer tlc, File ... gfile) {
        Set<String> gcontent = new HashSet();
        for(File f : gfile) {
            gcontent.add(FileUtils.loadFileContent(f.getAbsolutePath()));
        }
        init(gcontent,tlc);
    }

    /**
     * constructor
     *
     * @param gfile List of antlr grammar files
     */
    public GenericParser(File ... gfile) {
        Set<String> gcontent = new HashSet();
        for(File f : gfile) {
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

    /**
     * compile grammar file
     *
     * @return true if compilation was successful, false otherwise
     */
    public boolean compile() {
        LOGGER.debug("compile");
        // the antlr objects are already compiled
        if (antrlObjectsAvailable())
            return false;

        String tokvoc = "";


        StringCodeGenPipeline last = null;
        for(StringCodeGenPipeline p : gen) {

            Grammar g = p.getG();

            if(last != null && last.hasTokenVocab()) {
                tokvoc = last.getTokenVocabString();
                if(g instanceof InmemantlrGrammar) {
                    ((InmemantlrGrammar) g).setTokenVocab(tokvoc);
                    tokvoc = "";
                }
                else if (g instanceof InmemantlrLexerGrammar) {
                    ((InmemantlrLexerGrammar) g).setTokenVocab(tokvoc);
                    tokvoc = "";
                }
            }

            // process grammar
            antlr.process(g);
            // process string code gen pipeline afterwards
            p.process();
            if(!sc.compile(gen))
                return false;

            last = p;
        }

        LOGGER.debug("compile {} elements", gen.size());
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
            throw new FileNotFoundException("cound not find file " + toParse
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
