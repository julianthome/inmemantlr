/**
 * Inmemantlr - In memory compiler for Antlr 4
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.GenericParser.CaseSensitiveType;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.stream.CasedStreamProvider;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


public class TestExternalGrammars {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExternalGrammars.class);

    private static String[] special = {
            "antlr/antlr4", // antlr4 is handled by an extra test case
            "aspectj", // have to manually adapt Java grammar
            "csharp", // csharp is handled by an extra test case
            "objc", // objc is handled by an extra test case
            "php",  // php is handled by an extra test case
            "stringtemplate",  // stringtemplate is handled by an extra case
            "python2", // seems to be broken
            "python3", // seems to be broken
            "swift/swift2", // handled by extra testcase
            "swift/swift3", // handled by extra testcase
            "swift-fin",
            "z", // handled by extra testcase
            "tsql", // handled by extra testcase
            "plsql", // handled by extra testcase
            "mysql", // handled by extra testcase
            "html", // handled by extra testcase
            "r", // handled by extra testcase

            "algol60", // broken at the mom
            "databank", //skip
            "javascript", // skip
            "antlr3", // skip
            "python3alt", //skip
            "python3-py",// skip
            "python3-cs",// skip
            "solidity", //skip
            "typescript", //skip
            "powerbuilder", //skip
            "golang" //skip
    };


    static File grammar = null;

    private Set<String> specialCases = new HashSet(Arrays.asList(special));

    private static Map<String, Subject> subjects = new HashMap<>();

    private static class Subject {

        public String name = "";
        public Set<File> g4 = new HashSet();
        // positive examples
        public Set<File> examples = new HashSet();

        // negative examples
        public Set<File> nexamples = new HashSet();
        public Map<String, String> errors = new HashMap();

        private String entrypoint = "";

        public boolean hasExamples() {
            return !examples.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("name:");
            sb.append(name);
            sb.append("\n");
            sb.append("g4 files:\n");
            g4.forEach(file -> {
                sb.append(file.getAbsolutePath());
                sb.append("\n");
            });
            if (hasExamples()) {
                sb.append("examples:\n");
                examples.forEach(file -> {
                    sb.append(file.getAbsolutePath());
                    sb.append("\n");
                });
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    private GenericParser getParserForSubject(Subject s, ToolCustomizer tc) {


        File[] gs = s.g4.toArray(new File[0]);

        LOGGER.debug("gs {}", gs.length);

        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(tc, gs));

        assertNotNull(gp);

        return gp;
    }

    private void verify(GenericParser g, Set<File> ok, Set<File> error) {
        verify(g, ok, null, CaseSensitiveType.NONE, true);
        verify(g, error, null, CaseSensitiveType.NONE, false);
    }

    private void verify(GenericParser p, Set<File> ok, Set<File> error, String ep) {
        DefaultTreeListener dt = new DefaultTreeListener();
        verify(p, ok, ep, CaseSensitiveType.NONE, true);
        verify(p, error, ep, CaseSensitiveType.NONE, false);
    }

    private void verify(GenericParser p, Set<File> toParse, String ep,
                        CaseSensitiveType t, boolean shouldParse) {
        DefaultTreeListener dt = new DefaultTreeListener();
        p.setListener(dt);

        boolean parses;

        for (File e : toParse) {
            try {
                LOGGER.info("parseFile {} with {} and ept {}", e.getName(), p
                        .getParserName(), ep);
                ParserRuleContext ctx = (ep != null && !ep.isEmpty()) ? p
                        .parse(e, ep, CaseSensitiveType.NONE) :
                        p.parse(e, t);
                //Assert.Assertions.assertNotNull(ctx);
                if (ctx == null)
                    parses = false;
                else
                    parses = true;
            } catch (IllegalWorkflowException | FileNotFoundException |
                    RecognitionException | ParsingException e1) {
                LOGGER.error("error: {}", e1.getMessage());
                parses = false;
            }

            assertEquals(shouldParse, parses);


            if (shouldParse) {
                ParseTree parseTree = dt.getParseTree();
                assertNotNull(parseTree);
                LOGGER.debug(parseTree.toDot());
                assertTrue(parseTree.getNodes().size() > 1);
            }
        }
    }

    static {
        if (TestExternalGrammars.class.getClassLoader().getResource("grammars-v4") == null)
            fail();

        grammar = new File(TestExternalGrammars.class.getClassLoader().getResource("grammars-v4").getFile());

        File[] files = grammar.listFiles(File::isDirectory);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


        for (File f : files) {


            assertTrue(f.isDirectory());
            Subject subject = new Subject();
            subject.name = f.getName();


            File[] xmla = f.listFiles(pathname -> pathname
                    .getName()
                    .equals("pom.xml"));

            List<File> xmls = Arrays.asList(xmla);

            if (xmls.size() == 1) {
                Document doc = null;
                try {
                    doc = dbf.newDocumentBuilder().parse(xmls.get(0));
                } catch (ParserConfigurationException e) {
                    fail();
                } catch (SAXException e) {
                    fail();
                } catch (IOException e) {
                    fail();
                }

                NodeList nl = doc.getElementsByTagName("entryPoint");

                if (nl.getLength() == 1) {
                    subject.entrypoint = nl.item(0).getTextContent();
                }

            }


            File[] gs = f.listFiles(pathname -> pathname.getName().endsWith(".g4"));


            if (gs != null && gs.length > 0) {
                subject.g4.addAll(Arrays.asList(gs));
            }

            File examples = new File(f.getAbsolutePath() + "/examples");

            File[] xamples = examples.listFiles(pathname -> !pathname
                    .isDirectory() && !FilenameUtils.getExtension(pathname
                    .getName()).equals("tree") &&
                    !FilenameUtils.getExtension(pathname.getName()).equals("errors") &&
                    !FilenameUtils.getName(pathname.getName()).contains
                            ("form1.vb")

            );

            File[] errors = examples.listFiles(pathname -> !pathname
                    .isDirectory() &&
                    FilenameUtils.getExtension(pathname.getName()).equals("errors")
            );

            if (errors != null) {
                for (File ef : errors) {
                    String content = FileUtils.loadFileContent(ef);
                    subject.errors.put(FilenameUtils.getBaseName(ef.getName()), content);
                }
            }

            if (xamples != null && xamples.length > 0) {
                subject.examples.addAll(Arrays.asList(xamples));
                Set<File> negative = subject.examples.stream().filter(x ->
                        subject.errors.keySet().contains(x.getName()))
                        .collect(Collectors.toSet());
                subject.examples.removeAll(negative);
                subject.nexamples.addAll(negative);
            }


            subjects.put(subject.name, subject);
        }
    }

    private void testSubject(Subject s, boolean skip) {
        LOGGER.debug("test {}", s.name);
        if (specialCases.contains(s.name) && skip) {
            LOGGER.debug("skip {}", s.name);
            return;
        }

        LOGGER.info("test {}", s.name);
        GenericParser gp = getParserForSubject(s, null);

        assertDoesNotThrow(gp::compile);
        LOGGER.debug("successfully compiled grammar");

        DefaultTreeListener dt = new DefaultTreeListener();
        gp.setListener(dt);

        verify(gp, s.examples, s.nexamples, s.entrypoint);

    }

    @Test
    public void testGeneration() {
        subjects.values().stream().filter(Subject::hasExamples).
                forEach(s -> testSubject(s, true));
    }

    private boolean toCheck(String tcase) {
        if (subjects.containsKey(tcase))
            return true;

        return false;
    }

    @Test
    public void testAntlr4() {

        if (!toCheck("antlr4"))
            return;

        Subject s = subjects.get("antlr4");

        // Exam
        ToolCustomizer tc = t -> t.genPackage = "org.antlr.parser.antlr4";

        Set<File> files = s.g4.stream().filter(v -> v.getName().matches("(ANTLRv4(Lexer|Parser)|LexBasic).g4")).collect(Collectors.toSet());

        assertTrue(!files.isEmpty());

        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(tc, files.toArray(new File[0])));

        DefaultTreeListener dt = new DefaultTreeListener();

        gp.setListener(dt);

        assertDoesNotThrow(() -> gp.addUtilityJavaFiles(new File("src/test/resources/grammars-v4/antlr4/src/main/java/org/antlr/parser/antlr4/LexerAdaptor.java")));

        assertDoesNotThrow(gp::compile);

        s.examples.removeIf(f -> f.getName().contains("three.g4"));

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testStringTemplate() {

        // skip for the time being -- grammar issue

        if (!toCheck("stringtemplate"))
            return;

        Subject s = subjects.get("stringtemplate");

        //LOGGER.info("G4 {}", s.g4);

        // Exam
        ToolCustomizer tc = t -> t.genPackage = "org.antlr.parser.st4";

        GenericParser gp = getParserForSubject(s, tc);

        DefaultTreeListener dt = new DefaultTreeListener();

        gp.setListener(dt);


        assertDoesNotThrow(() -> {
            File util = new File
                    ("src/test/resources/grammars-v4/stringtemplate/" +
                            "src/main/java/org/antlr/parser/st4/LexerAdaptor.java");
            gp.addUtilityJavaFiles(util);
        });

        assertDoesNotThrow(gp::compile);


        LOGGER.debug("name {}", gp.getParserName());
        gp.setParserName("org.antlr.parser.st4.STParser");
        gp.setLexerName("org.antlr.parser.st4.STLexer");

        s.examples = s.examples.stream().filter(f -> f.getName().contains
                ("example1.st")
        ).collect(Collectors.toSet());

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testSwift2() {

        if (!toCheck("swift2"))
            return;

        Subject s = subjects.get("swift2");

        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(s.g4.toArray(new File[0])));

        assertDoesNotThrow(() -> {
            File util = new File
                    ("src/test/resources/grammars-v4/swift2/src/main/java" +
                            "/SwiftSupport.java");
            gp.addUtilityJavaFiles(util);
        });

        assertDoesNotThrow(gp::compile);

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testSwift3() {

        if (!toCheck("swift3"))
            return;

        Subject s = subjects.get("swift3");

        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(s.g4.toArray(new File[0])));

        assertDoesNotThrow(() -> {
            File util = new File
                    ("src/test/resources/grammars-v4/swift3/src/main/java" +
                            "/SwiftSupport.java");
            gp.addUtilityJavaFiles(util);
        });

        assertDoesNotThrow(gp::compile);

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }


    @Test
    public void testEcma() {

        if (!toCheck("ecmascript"))
            return;

        Subject s = subjects.get("ecmascript");

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        assertDoesNotThrow(gp::compile);

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testZ() {

        if (!toCheck("z"))
            return;
        Subject s = subjects.get("z");


        //Assertions.assertTrue(mfiles.size() > 0);

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        try {
            File util1 = new File
                    ("src/test/resources/grammars-v4/z/src/main/java" +
                            "/ZOperatorListener.java");
            File util2 = new File
                    ("src/test/resources/grammars-v4/z/src/main/java" +
                            "/ZSupport.java");

            gp.addUtilityJavaFiles(util1, util2);

        } catch (FileNotFoundException e) {
            fail();
        }

        assertNotNull(gp);

        DefaultTreeListener mdt = new DefaultTreeListener();

        assertDoesNotThrow(gp::compile);

        gp.setListener(mdt);

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }


    @Test
    public void testObjc() {

        if (!toCheck("objc"))
            return;

        Subject s = subjects.get("objc");

        s.g4.add(new File("src/test/resources/grammars-v4/objc/one-step" +
                "-processing/ObjectiveCLexer.g4"));
        s.g4.add(new File("src/test/resources/grammars-v4/objc/one-step" +
                "-processing/ObjectiveCPreprocessorParser.g4"));

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        DefaultTreeListener mdt = new DefaultTreeListener();

        assertDoesNotThrow(gp::compile);

        gp.setListener(mdt);

        s.examples.removeIf(p -> p.getName().contains("AllInOne.m"));

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }


    @Test
    public void testCSharp() {

        // skipt this test if the runtime environment is windows for the time
        // being -- csharp grammar is runtime dependent
        if (System.getProperty("os.name").toLowerCase().startsWith("win"))
            return;

        if (!toCheck("csharp"))
            return;

        Subject s = subjects.get("csharp");

        Set<File> mfiles = s.g4.stream().filter(v -> v.getName().matches(
                "CSharp" + "(Lexer|PreprocessorParser|Parser).g4")).collect
                (Collectors.toSet());

        assertFalse(mfiles.isEmpty());

        GenericParser mparser = null;
        try {
            mparser = new GenericParser(mfiles.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        assertNotNull(mparser);

        DefaultTreeListener mdt = new DefaultTreeListener();

        assertDoesNotThrow(mparser::compile);

        mparser.setListener(mdt);

        mparser.setParserName("CSharpParser");

        verify(mparser, s.examples, s.nexamples, s.entrypoint);
    }


    @Test
    public void testTSql() {

        if (!toCheck("tsql"))
            return;

        Subject s = subjects.get("tsql");

        Set<File> mfiles = s.g4.stream().filter(v -> v.getName().matches("TSql(Lexer|Parser).g4")).collect
                (Collectors.toSet());

        assertFalse(mfiles.isEmpty());

        GenericParser mparser = null;
        try {
            mparser = new GenericParser(mfiles.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        assertNotNull(mparser);

        DefaultTreeListener mdt = new DefaultTreeListener();

        assertDoesNotThrow(mparser::compile);
        mparser.setStreamProvider(new CasedStreamProvider(CaseSensitiveType.UPPER));

        mparser.setListener(mdt);

        // seems to cause issues when running on windows
        s.examples.removeIf(f -> f.getName().equals("full_width_chars.sql"));

        verify(mparser, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testMySQL() {

        if (!toCheck("mysql"))
            return;

        Subject s = subjects.get("mysql");

        Set<File> mfiles = s.g4.stream().filter(v -> v.getName().matches(
                "MySql" + "(Lexer|Parser).g4")).collect
                (Collectors.toSet());

        assertFalse(mfiles.isEmpty());

        GenericParser mparser = null;
        try {
            mparser = new GenericParser(mfiles.toArray(new File[0]));
        } catch (FileNotFoundException e) {
            fail();
        }


        assertNotNull(mparser);

        DefaultTreeListener mdt = new DefaultTreeListener();

        assertDoesNotThrow(mparser::compile);

        mparser.setStreamProvider(new CasedStreamProvider(CaseSensitiveType.UPPER));

        mparser.setListener(mdt);

        verify(mparser, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testHTML() {

        if (!toCheck("html"))
            return;

        Subject s = subjects.get("html");

        GenericParser gp = getParserForSubject(s, null);

        assertDoesNotThrow(gp::compile);

        s.examples.removeIf(p -> !p.getName().contains("antlr"));
        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }

    @Test
    public void testR() {


        if (!toCheck("r"))
            return;

        Subject s = subjects.get("r");

        GenericParser gp = getParserForSubject(s, null);

        assertDoesNotThrow(gp::compile);

        gp.setParserName("RParser");

        verify(gp, s.examples, s.nexamples, s.entrypoint);
    }
}
