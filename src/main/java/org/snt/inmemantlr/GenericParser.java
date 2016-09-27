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

/**
 * generic parser - an antlr parser representation
 */
public class GenericParser {

    final static Logger logger = LoggerFactory.getLogger(GenericParser.class);

    private Tool antlr;
    private String cname;
    private Grammar g = null;
    private StringCodeGenPipeline gen;
    private DefaultListener listener;
    private StringCompiler sc;
    private File gfile;
    private String gconent;


    /**
     * constructor
     * @param grammarFile path to grammar file
     * @param name parser name
     */
    public GenericParser(File grammarFile, String name) {
        this(grammarFile, name, null);
    }

    /**
     * constructor
     * @param content grammar file content
     * @param name parser name
     */
    public GenericParser(String content, String name) {
        this(content,name, null);
    }

    /**
     * constructor
     * @param grammarFile grammar file
     * @param name grammar name
     * @param tlc a ToolCustomizer
     */
    public GenericParser(File grammarFile, String name, ToolCustomizer tlc) {
        /**this.antlr = new Tool();
        this.cname = name;
        this.gfile = grammarFile;
        this.gconent = FileUtils.loadFileContent(grammarFile.getAbsolutePath());
        assert (grammarFile.exists());
        this.g = antlr.loadGrammar(grammarFile.getAbsolutePath());
        this.gen = new StringCodeGenPipeline(g, cname);
        this.sc = new StringCompiler();**/
        this(FileUtils.loadFileContent(grammarFile.getAbsolutePath()), name, tlc);
        this.gfile = grammarFile;
    }

    /**
     * constructor
     * @param content grammar file content
     * @param name grammar
     * @param tlc a ToolCustomizer
     */
    public GenericParser(String content, String name, ToolCustomizer tlc) {
        this.antlr = new Tool();
        if (tlc != null) {
            tlc.customize(this.antlr);
        }
        this.cname = name;
        this.gconent = content;
        this.g = loadGrammarFromString(content, name);
        this.gen = new StringCodeGenPipeline(g, cname);
        this.sc = new StringCompiler();
    }


    /**
     * compile grammar file
     * @return true if compilation was succesful, false otherwise
     */
    public boolean compile() {

        // the antlr objects are already compiled
        if(antrlObjectsAvailable())
            return false;

        logger.debug("compiled");
        gen.process();
        return this.sc.compile(gen);
    }

    /**
     * load antlr grammar from string
     * @param content string content from antlr grammar
     * @param name name of antlr grammar
     * @return grammar object
     */
    public Grammar loadGrammarFromString(String content, String name) {
        GrammarRootAST grammarRootAST = this.antlr.parseGrammarFromString(content);
        final Grammar g = this.antlr.createGrammar(grammarRootAST);
        g.fileName = name;
        this.antlr.process(g, false);
        return g;
    }

    /**
     * parse string an create a context
     * @param toParse string to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse) throws IllegalWorkflowException {

        if (listener == null) {
            this.listener = new DefaultListener();
        }

        this.listener.reset();

        if (!antrlObjectsAvailable()) {
            throw new IllegalWorkflowException("No antlr objects have been compiled or loaded");
        }

        ANTLRInputStream input = new ANTLRInputStream(toParse);

        Lexer lex = this.sc.instanciateLexer(input, cname);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        tokens.fill();

        Parser parser = this.sc.instanciateParser(tokens, cname);
        parser.reset();


        // make parser information available to listener
        this.listener.setParser(parser);

        parser.addErrorListener(new DiagnosticErrorListener());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.setBuildParseTree(true);
        parser.setTokenStream(tokens);


        String[] rules = parser.getRuleNames();
        String EntryPoint = rules[0];
        ParserRuleContext data = null;

        try {
            Class<?> pc = parser.getClass();
            Method m = pc.getMethod(EntryPoint, (Class<?>[]) null);
            data = (ParserRuleContext) m.invoke(parser, (Object[]) null);
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return null;
        }

        // System.out.println(data.toStringTree(parser));
        // System.out.println(data.toInfoString(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(this.listener, data);

        return data;
    }

    /**
     * parse in fresh
     * @param toParse string to parse
     * @param listener a ParseTreeListener
     * @param production Production name to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     */
    public ParserRuleContext parse(String toParse, DefaultListener listener, String production) throws IllegalWorkflowException {

        if (listener == null) {
            this.listener = new DefaultListener();
        }
        if (!antrlObjectsAvailable()) {
            throw new IllegalWorkflowException("No antlr objects have been compiled or loaded");
        }

        ANTLRInputStream input = new ANTLRInputStream(toParse);

        Lexer lex = this.sc.instanciateLexer(input, cname);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        tokens.fill();

        Parser parser = this.sc.instanciateParser(tokens, cname);

        // make parser information available to listener
        listener.setParser(parser);

        //parser.addErrorListener(new DiagnosticErrorListener());
        parser.removeErrorListeners();
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.setBuildParseTree(true);
        parser.setTokenStream(tokens);

        String[] rules = parser.getRuleNames();
        String EntryPoint = null;
        if (production == null) {
            EntryPoint = rules[0];
        } else {
            if (!Arrays.asList(rules).contains(production))
                throw new IllegalArgumentException(String.format("Rule %s not found", production));
            EntryPoint = production;
        }

        ParserRuleContext data = null;
        try {
            Class<?> pc = parser.getClass();
            Method m = pc.getMethod(EntryPoint, (Class<?>[]) null);
            data = (ParserRuleContext) m.invoke(parser, (Object[]) null);
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            return null;
        }

        // System.out.println(prc.toStringTree(parser));
        // System.out.println(prc.toInfoString(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(listener, data);

        return data;
    }

    /**
     * get parse tree listener
     * @return parse tree listener
     */
    public ParseTreeListener getListener() {
        return this.listener;
    }

    /**
     * set parse tree listener
     * @param listener listener to use
     */
    public void setListener(DefaultListener listener) {
        this.listener = listener;
    }

    /**
     * get antlr grammar object
     * @return antlr grammar object
     */
    public Grammar getGrammar() {
        return this.g;
    }

    /**
     * get all compiled antlr objects (lexer, parser, etc) in source and
     * bytecode format
     * @return memory tuple set
     */
    public MemoryTupleSet getAllCompiledObjects() {
        return this.sc.getAllCompiledObjects();
    }

    public boolean antrlObjectsAvailable() {
        return getAllCompiledObjects().size() > 0;
    }


    public void store(String file, boolean overwrite) throws SerializationException {

        File loc = new File(file);
        File path = loc.getParentFile();

        logger.debug("store file " + loc.getAbsolutePath());

        if(loc.exists() && !overwrite) {
            throw new SerializationException("File " + file + " already exists");
        }

        if(!path.exists()) {
            throw new SerializationException("Cannot find path " + path.getAbsolutePath());
        }

        if(!antrlObjectsAvailable()) {
            throw new SerializationException("You have not compiled your grammar yet -- there" +
                    "are no antlr objects availabe");
        }

        FileOutputStream f_out = null;
        ObjectOutputStream o_out = null;

        try {
            f_out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new SerializationException("output file cannot be found");
        }

        try {
            o_out = new ObjectOutputStream (f_out);
        } catch (IOException e) {
            throw new SerializationException("object output stream cannot be created");
        }

        GenericParserSerialize towrite = new GenericParserSerialize(this.gfile,
                this.gconent,
                getAllCompiledObjects(),
                this.cname);

        try {
            o_out.writeObject(towrite);
        } catch (NotSerializableException e) {
            logger.error("Not serializable " + e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SerializationException("error occurred while writing object");
        }

    }

    public static GenericParser load(String file) throws DeserializationException {

        File loc = new File(file);
        File path = loc.getParentFile();

        if(!loc.exists()) {
            throw new DeserializationException("File " + file + " does not exist");
        }

        if(!path.exists()) {
            throw new DeserializationException("Cannot find path " + path.getAbsolutePath());
        }

        logger.debug("load file " + loc.getAbsolutePath());

        FileInputStream f_in = null;
        ObjectInputStream o_in = null;

        try {
            f_in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DeserializationException("input file " + file + " cannot be found");
        }

        try {
            o_in = new ObjectInputStream (f_in);
        } catch (IOException e) {
            throw new DeserializationException("object input stream cannot be found");
        }

        Object toread = null;

        try {
            toread = o_in.readObject();
        } catch (NotSerializableException e) {
            throw new DeserializationException("cannot read object: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new DeserializationException("cannot find class: " + e.getMessage());
        } catch (IOException e) {
            throw new DeserializationException("io exception: " + e.getMessage());
        }

        assert(toread instanceof GenericParserSerialize);
        GenericParserSerialize gin = (GenericParserSerialize) toread;

        GenericParser gp = null;

        if(gin.getGrammarFile() != null && gin.getGrammarFile().exists()) {
            gp = new GenericParser(gin.getGrammarFile(), gin.getCname());
        } else if(gin.getGrammarContent() != null && gin.getGrammarContent().length() > 0) {
            gp = new GenericParser(gin.getGrammarContent(), gin.getCname(), null);
        } else {
            throw new DeserializationException("cannot deserialize " + file);
        }

        assert(gp != null);

        gp.sc.load(gin.getMemoryTupleSet());

        if(!gp.antrlObjectsAvailable()) {
            throw new DeserializationException("there are no antlr objects available in " + file);
        }

        return gp;
    }

}
