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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tool.ToolCustomizer;
import org.snt.inmemantlr.tree.Ast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestExternalGrammars {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExternalGrammars.class);

    private static String[] special = {
            "antlr3", // antlr3 grammar seems to be broken
            "antlr4", // antlr4 is handled by an extra test case
            "aspectj", // have to manually adapt Java grammar
            "csharp", // csharp requires extra runtime
            "ecmascript", // ecmascript is handled by an extra test case
            "objc", // objc is handled by an extra test case
            "php",  // php is handled by an extra test case
            "stringtemplate",  // stringtemplate is handled by an extra case
            "swift",  // swift is handled by an extra test case
            "swift-fin" //swift-fin can be ignored
    };

    static File grammar = null;

    private Set<String> blacklist = new HashSet(Arrays.asList(special));

    private Map<String, Subject> subjects = null;

    private static class Subject {

        public String name = "";
        public Set<File> g4 = new HashSet();
        public Set<File> examples = new HashSet();

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
        GenericParser gp = null;
        try {
            gp = new GenericParser(tc, s.g4.toArray(new File[s.g4.size()]));
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }

        Assert.assertNotNull(gp);

        return gp;
    }

    private void verify(GenericParser p, Set<File> toParse) {
        DefaultTreeListener dt = new DefaultTreeListener();
        p.setListener(dt);
        toParse.forEach(e -> {
            try {
                LOGGER.info("parse {}", e.getName());
                p.parse(e);
            } catch (IllegalWorkflowException | FileNotFoundException e1) {
                LOGGER.error(e1.getMessage());
                assertFalse(true);
            }

            Ast ast = dt.getAst();
            Assert.assertNotNull(ast);
            assertTrue(ast.getNodes().size() > 1);
        });
    }

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();

        if (classLoader.getResource("grammars-v4") == null)
            return;

        grammar = new File(classLoader.getResource("grammars-v4").getFile());

        File[] files = grammar.listFiles(File::isDirectory);

        subjects = new HashMap<>();

        for (File f : files) {
            assertTrue(f.isDirectory());
            Subject subject = new Subject();
            subject.name = f.getName();

            File[] gs = f.listFiles(pathname -> pathname.getName().endsWith(".g4"));
            if (gs != null && gs.length > 0)
                subject.g4.addAll(Arrays.asList(gs));

            File examples = new File(f.getAbsolutePath() + "/examples");

            File[] xamples = examples.listFiles(pathname -> !pathname.isDirectory());

            if (xamples != null && xamples.length > 0)
                subject.examples.addAll(Arrays.asList(xamples));

            subjects.put(subject.name, subject);
        }
    }

    private void testSubject(Subject s, boolean skip) {
        if (blacklist.contains(s.name) && skip) {
            LOGGER.debug("skip {}", s.name);
            return;
        }

        LOGGER.info("test {}", s.name);
        GenericParser gp = getParserForSubject(s, null);
        assertTrue(gp.compile());
        LOGGER.debug("successfully compiled grammar");

        DefaultTreeListener dt = new DefaultTreeListener();
        gp.setListener(dt);

        verify(gp, s.examples);

    }

    @Test
    public void testGeneration() {
        subjects.values().stream().filter(Subject::hasExamples).
                forEach(s -> testSubject(s, true));
    }

    @Test
    public void testAntlr4() {
        Subject s = subjects.get("antlr4");

        // Exam
        ToolCustomizer tc = t -> t.genPackage = "org.antlr.parser.antlr4";

        Set<File> files = s.g4.stream().filter(v -> v.getName().matches("" +
                "(ANTLRv4" +
                "(Lexer|Parser)|LexBasic).g4")).collect(Collectors.toSet());

        assertTrue(files.size() > 0);

        GenericParser gp = null;
        try {
            gp = new GenericParser(tc, files.toArray(new File[files
                    .size()]));
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }

        DefaultTreeListener dt = new DefaultTreeListener();

        gp.setListener(dt);

        try {
            File util = new File
                    ("src/test/resources/grammars-v4/antlr4/src/main/java" +
                            "/org" +
                            "/antlr/parser/antlr4/LexerAdaptor.java");
            gp.addUtilityJavaFiles(util);
        } catch (FileNotFoundException e) {
            assertFalse(true);
        }

        gp.compile();

        verify(gp, s.examples);
    }

    @Test
    public void testStringTemplate() {
        Subject s = subjects.get("stringtemplate");

        // Exam
        ToolCustomizer tc = t -> t.genPackage = "org.antlr.parser.st4";

        GenericParser gp = getParserForSubject(s, tc);

        DefaultTreeListener dt = new DefaultTreeListener();

        gp.setListener(dt);

        try {
            File util = new File
                    ("src/test/resources/grammars-v4/stringtemplate/" +
                            "src/main/java/org/antlr/parser/st4/LexerAdaptor.java");
            gp.addUtilityJavaFiles(util);
        } catch (FileNotFoundException e) {
            assertFalse(true);
        }

        gp.compile();

        verify(gp, s.examples);
    }

    @Test
    public void testSwift() {
        Subject s = subjects.get("swift");

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[s.g4.size()]));
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }

        try {
            File util = new File
                    ("src/test/resources/grammars-v4/swift/src/main/java" +
                            "/SwiftSupport.java");
            gp.addUtilityJavaFiles(util);
        } catch (FileNotFoundException e) {
            assertFalse(true);
        }

        gp.compile();

        verify(gp, s.examples);
    }

    @Test
    public void testObjC() {
        Subject s = subjects.get("objc");

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[s.g4.size()]));
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }
        gp.compile();
        verify(gp, s.examples);
    }

    @Test
    public void testPHP() {
        Subject s = subjects.get("php");

        s.g4.removeIf(f -> f.getName().equals("PHPLexer_CSharpSharwell.g4") ||
        f.getName().equals("PHPLexer_Python.g4"));

        GenericParser gp = null;
        try {
            gp = new GenericParser(s.g4.toArray(new File[s.g4.size()]));
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }

        gp.compile();

        verify(gp, s.examples);
    }

    @Test
    public void testEcmaScript() {
        Subject s = subjects.get("ecmascript");
        s.g4.removeIf(f -> !f.getName().equals("ECMAScript.g4"));
        testSubject(s, false);
    }
}
