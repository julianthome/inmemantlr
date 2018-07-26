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
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.comp.*;
import org.snt.inmemantlr.exceptions.*;
import org.snt.inmemantlr.listener.DefaultListener;
import org.snt.inmemantlr.memobjects.GenericParserSerialize;
import org.snt.inmemantlr.memobjects.MemorySource;
import org.snt.inmemantlr.memobjects.MemoryTuple;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.stream.DefaultStreamProvider;
import org.snt.inmemantlr.stream.StreamProvider;
import org.snt.inmemantlr.tool.InmemantlrErrorListener;
import org.snt.inmemantlr.tool.InmemantlrErrorListener.Type;
import org.snt.inmemantlr.tool.InmemantlrTool;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.utils.FileUtils;
import org.snt.inmemantlr.utils.Tuple;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * generic parser
 */
public class GenericParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParser.class);

    public enum CaseSensitiveType {
        NONE,
        UPPER,
        LOWER
    }

    private InmemantlrTool antlr = new InmemantlrTool();
    private DefaultListener listener = new DefaultListener();
    private StringCompiler sc = new StringCompiler();
    private CompilerOptionsProvider oprov = new
            DefaultCompilerOptionsProvider();

    private FileProvider fp = new FileProvider();
    private StreamProvider provider = new DefaultStreamProvider();
    private boolean useCached = true;
    private String lexerName = "";
    private String parserName = "";


    /**
     * initialize the generic parser
     *
     * @param gcontent content of grammar files
     * @param tlc tool customizer
     */
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

    /**
     * private constructor used to reconstruct a serialized generic parser
     *
     * @param mset set of memory tuples
     * @param parserName parser name
     * @param lexerName lexer name
     */
    private GenericParser(MemoryTupleSet mset, String parserName, String
            lexerName) {
        if (mset == null || mset.size() == 0)
            throw new IllegalArgumentException("mset must not be null or empty");

        sc.load(mset);
        LOGGER.debug("parser ", parserName);
        LOGGER.debug("parser ", lexerName);
        this.parserName = parserName;
        this.lexerName = lexerName;
    }

    /**
     * add utility Java classes on which the antlr grammar depend on
     *
     * @param fils list of Java files
     * @throws FileNotFoundException Java file does not exist
     */
    public void addUtilityJavaFiles(File... fils) throws
            FileNotFoundException {
        for (File f : fils) {
            addUtilityJavaFile(f);
        }
    }

    /**
     * add utility Java classes on which the antlr grammar depend on
     *
     * @param fils list of paths to Java files
     * @throws FileNotFoundException Java file does not exist
     */
    public void addUtilityJavaFiles(String... fils) throws FileNotFoundException {
        for (String f : fils) {
            addUtilityJavaFile(new File(f));
        }
    }

    /**
     * add utility Java class on which the antlr grammar depends on
     *
     * @param f Java file
     * @throws FileNotFoundException Java file does not exist
     */
    private void addUtilityJavaFile(File f) throws
            FileNotFoundException {

        if (!f.exists()) {
            throw new FileNotFoundException("File " + f.getName() + " does " +
                    "not exist");
        }

        String name = FilenameUtils.removeExtension(f
                .getName());
        String content = FileUtils.loadFileContent(f);


        LOGGER.debug("add utility {}", name);
        fp.addFiles(new MemorySource(name, content));
    }

    /**
     * constructor
     *
     * @param gcontent List of antlr grammar content
     * @param tlc      a ToolCustomizer
     */
    public GenericParser(ToolCustomizer tlc, String... gcontent) {
        init(new HashSet<>(Arrays.asList(gcontent)), tlc);
    }

    /**
     * constructor
     *
     * @param gcontent List of antlr grammar content
     */
    public GenericParser(String... gcontent) {
        init(new HashSet<>(Arrays.asList(gcontent)), null);
    }

    /**
     * constructor
     *
     * @param gfile List of antlr grammar files
     * @param tlc   a ToolCustomizer
     * @throws FileNotFoundException file not found
     */
    public GenericParser(ToolCustomizer tlc, File... gfile) throws FileNotFoundException {
        Set<String> gcontent = new HashSet<>();
        for (File f : gfile) {
            if (!f.exists() || !f.canRead())
                throw new FileNotFoundException("file " + f.getAbsolutePath()
                        + " does not exist or is not readable");

            gcontent.add(FileUtils.loadFileContent(f.getAbsolutePath()));
        }
        init(gcontent, tlc);
    }

    /**
     * File was not found
     *
     * @param gfile List of antlr grammar files
     * @throws FileNotFoundException file not found
     */
    public GenericParser(File... gfile) throws FileNotFoundException {
        if (gfile.length == 0)
            throw new IllegalArgumentException("Antlr grammar files must not be empty");

        Set<String> gcontent = new HashSet<>();
        for (File f : gfile) {
            if (!f.exists() || !f.canRead())
                throw new FileNotFoundException("file " + f.getAbsolutePath()
                        + " does not exist or is not readable");

            gcontent.add(FileUtils.loadFileContent(f.getAbsolutePath()));
        }
        init(gcontent, null);
    }

    /**
     * constructor
     *
     * @param content   grammar file content
     * @param tlc       a ToolCustomizer
     * @param useCached true to used cached lexers, otherwise false
     */
    public GenericParser(ToolCustomizer tlc, boolean useCached, String...
            content) {
        this(tlc, content);
        this.useCached = useCached;
    }

    /**
     * a single instance of a generic parser
     *
     * @param tlc     a ToolCustomizer
     * @param content grammar content
     * @return grammar object
     */
    public static GenericParser instance(ToolCustomizer tlc, String content) {
        return new GenericParser(tlc, content);
    }

    /**
     * independent instance of a generic parser
     * @param tlc a ToolCustomizer
     * @param content grammar content
     * @return grammar object
     */
    public static GenericParser independentInstance(ToolCustomizer tlc,
                                                    String content) {
        return new GenericParser(tlc, false, content);
    }

    /**
     * set classpath
     * @param cp list of items to add to classpath
     */
    public void setClassPath(Collection<String> cp) {
        oprov.setClassPath(cp);
    }


    /**
     * get char stream provider for lexer
     * @return stream provider
     */
    public StreamProvider getStreamProvider() {
        return provider;
    }

    /**
     * set char stream provider for lexer
     * @param provider stream provider
     */
    public void setStreamProvider(StreamProvider provider) {
        this.provider = provider;
    }

    public void setCompilerOptionsProvider(CompilerOptionsProvider oprov) {
        this.oprov = oprov;
    }

    /**
     * compile generic parser
     *
     * @throws CompilationErrorException an error during the Java compilation
     * occurs
     * @throws RedundantCompilationException objects are already compiled
     */
    public void compile() throws CompilationException {
        LOGGER.debug("compile");

        // the antlr objects are already compiled
        if (antrlObjectsAvailable())
            throw new RedundantCompilationException("Antlr objects are already " +
                    "available");


        Set<StringCodeGenPipeline> pip = antlr.getPipelines();

        if (pip.isEmpty())
            throw new CompilationException("No string code pipeline available");

        for (StringCodeGenPipeline p : pip) {
            for (MemorySource ms : p.getItems()) {
                LOGGER.debug(ms.getName() + " " + ms.toString());
            }
        }

        // process all grammar objects
        Tuple<String, String> parserLexer = antlr.process();

        parserName = parserLexer.getFirst();
        lexerName = parserLexer.getSecond();


        // parser name can be empty for lexer grammars
        if (lexerName.isEmpty())
            throw new IllegalArgumentException("lexerName must not be empty");


        Set<CunitProvider> cu = new LinkedHashSet<>();

        if (fp.hasItems())
            cu.add(fp);

        cu.addAll(antlr.getCompilationUnits());

        sc.compile(cu, oprov);
    }

    /**
     * parse file content an create a context
     *
     * @param toParse file to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws FileNotFoundException    if input file cannot be found
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(File toParse) throws
            IllegalWorkflowException, FileNotFoundException, ParsingException {
        return parse(toParse, null, CaseSensitiveType.NONE);
    }

    /**
     * parse file content an create a context
     *
     * @param toParse file to parse
     * @param cs set context sensitivity
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws FileNotFoundException    if input file cannot be found
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(File toParse, CaseSensitiveType cs) throws
            IllegalWorkflowException, FileNotFoundException, ParsingException {
        return parse(toParse, null, cs);
    }

    /**
     * parse file content an create a context
     *
     * @param toParse    file to parseFile
     * @param production production name to parseFile
     * @param cs         case sensitivity
     * @return context
     * @throws IllegalWorkflowException sources are not compiled
     * @throws FileNotFoundException    file not found
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(File toParse, String production,
                                   CaseSensitiveType cs) throws
            IllegalWorkflowException, FileNotFoundException, ParsingException {
        if (!toParse.exists()) {
            throw new FileNotFoundException("could not find file " + toParse
                    .getAbsolutePath());
        }

        return parse(FileUtils.loadFileContent(toParse.getAbsolutePath()),
                production, cs);
    }

    /**
     * parse string and create a context
     *
     * @param toParse string to parse
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(String toParse) throws
            IllegalWorkflowException, ParsingException {
        return parse(toParse, null, CaseSensitiveType.NONE);
    }

    /**
     * parse string and create a context
     *
     * @param toParse string to parse
     * @param cs         case sensitivity
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(String toParse, CaseSensitiveType cs) throws
            IllegalWorkflowException, ParsingException {
        return parse(toParse, null, cs);
    }


    /**
     * write antlr artifacts to destination
     * @param dest directory to which the artifacts should be written
     */
    public void writeAntlrAritfactsTo(String dest) {
        MemoryTupleSet ms = getAllCompiledObjects();

        for (MemoryTuple tup : ms) {
            MemorySource src = tup.getSource();

            try {
                FileUtils.writeStringToFile(src.getCharContent(false).toString(),
                        Paths.get(dest, src.getClassName()).toString() +
                                ".java");
            } catch (FileExistsException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }


    /**
     * get the tokens for a string -- only for lexer grammars
     * @param toParse string to be tokenized
     * @return a list of tokens
     * @throws IllegalWorkflowException in case no lexername is provided
     */
    public List<Token> lex(String toParse) throws IllegalWorkflowException {

        if (lexerName.isEmpty())
            throw new IllegalWorkflowException("lexerName must not be empty " +
                    "-- did you already run compile?");

        InmemantlrErrorListener el = new InmemantlrErrorListener();
        listener.reset();
        //CodePointCharStream input = CharStreams.fromString(toParse);
        CharStream input = provider.getCharStream(toParse);

        Objects.requireNonNull(input, "char stream must not be null");

        LOGGER.debug("load lexer {}", lexerName);

        Lexer lex = sc.instanciateLexer(input, lexerName, useCached);
        lex.addErrorListener(el);

        Objects.requireNonNull(lex, "lex must not be null");

        CommonTokenStream tokens = new CommonTokenStream(lex);
        tokens.fill();
        return tokens.getTokens();
    }


    /**
     * parse string and create a context
     *
     * @param toParse    string to parse
     * @param production production name to parseFile
     * @param cs         case sensitivity
     * @return context
     * @throws IllegalWorkflowException if compilation did not take place
     * @throws ParsingException         if an error occurs while parsing
     */
    public ParserRuleContext parse(String toParse, String production,
                                   CaseSensitiveType cs)
            throws
            IllegalWorkflowException, ParsingException {
        if (!antrlObjectsAvailable()) {
            throw new IllegalWorkflowException("No antlr objects have been compiled or loaded");
        }


        if (lexerName.isEmpty())
            throw new IllegalWorkflowException("lexerName must not be empty " +
                    "-- did you already run compile?");

        if (parserName.isEmpty())
            throw new IllegalWorkflowException("parserName must not be empty " +
                    "-- run lex() in case you would like to run the lexer " +
                    "only.");

        switch (cs) {
            case NONE:
                break;
            case UPPER:
                toParse = toParse.toUpperCase();
                break;
            case LOWER:
                toParse = toParse.toLowerCase();
                break;
        }

        InmemantlrErrorListener el = new InmemantlrErrorListener();

        listener.reset();

        CharStream input = provider.getCharStream(toParse);

        Objects.requireNonNull(input, "char stream must not be null");

        LOGGER.debug("load lexer {}", lexerName);

        Lexer lex = sc.instanciateLexer(input, lexerName, useCached);
        lex.addErrorListener(el);
        Objects.requireNonNull(lex, "lex must not be null");

        CommonTokenStream tokens = new CommonTokenStream(lex);

        tokens.fill();

        LOGGER.debug("load parser {}", parserName);
        Parser parser = sc.instanciateParser(tokens, parserName);

        Objects.requireNonNull(parser, "Parser must not be null");

        // make parser information available to listener
        listener.setParser(parser);


        parser.removeErrorListeners();
        parser.addErrorListener(el);
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

        ParserRuleContext data = null;
        try {
            Class<?> pc = parser.getClass();
            Method m = pc.getDeclaredMethod(entryPoint, (Class<?>[]) null);
            Objects.requireNonNull(m, "method should not be null");
            data = (ParserRuleContext) m.invoke(parser, (Object[]) null);
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            //e.printStackTrace();
            return null;
        }

        Set<String> msgs = el.getLog().entrySet()
                .stream()
                .filter(e -> e.getKey() == Type.SYNTAX_ERROR)
                .map(Entry::getValue)
                .collect(Collectors.toSet());


        if (!msgs.isEmpty()) {
            throw new ParsingException(String.join("", msgs));
        }

        ParseTreeWalker.DEFAULT.walk(listener, data);
        return data;
    }

    /**
     * get parseFile tree listener
     *
     * @return parseFile tree listener
     */
    public ParseTreeListener getListener() {
        return listener;
    }

    /**
     * set parseFile tree listener
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

    /**
     * serialize generic parser
     *
     * @param file path where generic parser is supposed to be stored
     * @param overwrite overwrite file
     * @throws SerializationException generic parser is not serializable
     */
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


        try (FileOutputStream fOut = new FileOutputStream(file); ObjectOutput oOut = new ObjectOutputStream(fOut)) {
            GenericParserSerialize towrite = new GenericParserSerialize(getAllCompiledObjects(), parserName, lexerName);
            oOut.writeObject(towrite);
        } catch (NotSerializableException e) {
            LOGGER.error("Not serializable:", e.getMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error("Output file cannot be found: {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("IO Error: {}", e.getMessage());
        }
    }

    /**
     * Get active lexer name
     * @return name of the active lexer
     */
    public String getLexerName() {
        return lexerName;
    }

    /**
     * set active lexer name
     * @param lexerName name of the lexer to be used
     */
    public void setLexerName(String lexerName) {
        this.lexerName = lexerName;
    }

    /**
     * get active parser name
     * @return parser name
     */
    public String getParserName() {
        return parserName;
    }

    /**
     * set active parser name
     * @param parserName name of the parser to be used
     */
    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    /**
     * load serialized generic parser
     *
     * @param file file of serialized generic parser file
     * @return the deserialized generic parser
     * @throws DeserializationException generic parser is not de-serializable
     */
    public static GenericParser load(String file) throws DeserializationException {
        File loc = new File(file);
        File path = loc.getParentFile();

        if (!path.exists()) {
            throw new DeserializationException("Cannot find path " + path.getAbsolutePath());
        }

        if (!loc.exists()) {
            throw new DeserializationException("File " + file + " does not exist");
        }


        LOGGER.debug("load file {}", loc.getAbsolutePath());

        try (FileInputStream f_in = new FileInputStream(file); ObjectInputStream o_in = new ObjectInputStream(f_in)) {
            Object toread = o_in.readObject();

            if (!(toread instanceof GenericParserSerialize))
                throw new IllegalArgumentException("toread must be an instance of GenericParserSerialize");

            GenericParserSerialize gin = (GenericParserSerialize) toread;

            GenericParser gp = new GenericParser(gin.getMemoryTupleSet(), gin
                    .getParserName(), gin.getLexerName());

            if (!gp.antrlObjectsAvailable()) {
                throw new DeserializationException("there are no antlr objects available in " + file);
            }

            return gp;
        } catch (FileNotFoundException e) {
            throw new DeserializationException(String.format("Problem loading %s", file), e);
        } catch (NotSerializableException e) {
            throw new DeserializationException("cannot read object", e);
        } catch (ClassNotFoundException e) {
            throw new DeserializationException("cannot find class", e);
        } catch (IOException e) {
            throw new DeserializationException(e.getMessage(), e);
        }
    }
}
