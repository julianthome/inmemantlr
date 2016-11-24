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

import org.antlr.v4.Tool;
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

import static org.junit.Assert.assertTrue;

public class TestExternalGrammars {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExternalGrammars.class);


    // @TODO: add distinct test cases for test modules
    // antlr3 grammar seems to be broken
    private static String[] exclude = {
            "antlr3", "antlr4", "aspectj", "csharp", "ecmascript",
            "objc", "oncrpc", "php", "sqlite", "sparql",
            "stringtemplate", "swift", "swift-fin", "xml",
            "pgn", "ucb-logo", "agc"
    };

    static File grammar = null;

    private Set<String> blacklist = new HashSet(
            Arrays.asList(exclude)
    );


    private static class Subject {
        public String name = "";
        public Set<File> g4 = new HashSet();
        public Set<File> examples = new HashSet();


        public boolean hasExamples() {
            return !examples.isEmpty();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("name:");
            sb.append(name);
            sb.append("\n");
            sb.append("g4 files:\n");
            g4.forEach( file -> {
                sb.append(file.getAbsolutePath());
                sb.append("\n");
            });
            if (hasExamples()) {
                sb.append("examples:\n");
                examples.forEach( file -> {
                    sb.append(file.getAbsolutePath());
                    sb.append("\n");
                });
            }
            sb.append("\n");
            return sb.toString();
        }
    }


    private Map<String, Subject> subjects = null;

    @Before
    public void init() {

        ClassLoader classLoader = getClass().getClassLoader();

        if (classLoader.getResource("grammars-v4") == null)
            return;

        grammar = new File(classLoader.getResource("grammars-v4")
                .getFile());


        File[] files = grammar.listFiles(pathname -> pathname.isDirectory());

        Arrays.stream(files);

        subjects = new HashMap();

        for (File f : files) {
            assertTrue(f.isDirectory());
            Subject subject = new Subject();
            subject.name = f.getName();

            File [] gs = f.listFiles(pathname -> pathname
                            .getName()
                            .endsWith(".g4"));

            if( gs != null && gs.length > 0)
                subject.g4.addAll(Arrays.asList(gs));

            File examples = new File(f.getAbsolutePath() + "/examples");

            File [] xamples = examples.listFiles(pathname
                            -> !pathname.isDirectory());

            if(xamples != null && xamples.length > 0)
                subject.examples.addAll(Arrays.asList(xamples));

            subjects.put(subject.name, subject);
        }
    }

    private void testSubject(Subject s) {


        if (blacklist.contains(s.name)) {
            LOGGER.debug("skip {}", s.name);
            return;
        }

        LOGGER.debug("test {}", s.name);
        GenericParser gp = null;
        try {
            LOGGER.debug("create parser with {} files", s.g4.size());
            gp = new GenericParser(s.g4.toArray(new File[s.g4.size()]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(gp.compile());
        LOGGER.debug("successfully compiled grammar");

        DefaultTreeListener dt = new DefaultTreeListener();
        gp.setListener(dt);

        if (s.hasExamples()) {
            for (File f : s.examples) {
                try {
                    LOGGER.debug("parse file {}", f.getAbsolutePath());
                    gp.parse(f);
                } catch (IllegalWorkflowException e) {
                    Assert.assertTrue(false);
                } catch (FileNotFoundException e) {
                    Assert.assertTrue(false);
                }
                Assert.assertNotNull(dt.getAst());
                Assert.assertNotEquals(dt.getNodes().size(), 0);
            }
        }

    }

    @Test
    public void testGeneration() {
        subjects.values().stream().filter(Subject::hasExamples).
                forEach(s -> testSubject(s));
    }

    @Test
    public void testAntlr4() {
        Subject s = subjects.get("antlr4");

        // Exam
        ToolCustomizer tc = new ToolCustomizer() {
            @Override
            public void customize(Tool t) {
                t.genPackage = "org.antlr.parser.antlr4";
            }
        };

        Set<File> files = s.g4.stream().filter(v -> v.getName().matches("" +
                "(ANTLRv4" +
                "(Lexer|Parser)|LexBasic).g4")).collect(Collectors.toSet());


        Assert.assertTrue(files.size() > 0);

        GenericParser gp = new GenericParser(tc,files.toArray(new File[files
                .size()]));


        DefaultTreeListener dt = new DefaultTreeListener();

        gp.setListener(dt);

        try {
            File util = new File(getClass().getResource("grammars-v4/antlr4/src/main/java/org/antlr/parser/antlr4/LexerAdaptor.java").getFile());
            gp.addUtilityJavaFile(util);
        } catch (FileNotFoundException e) {
            Assert.assertFalse(true);
        }

        gp.compile();


        s.examples.forEach(e -> {
            try {
                gp.parse(e);
            } catch (IllegalWorkflowException e1) {
                LOGGER.error(e1.getMessage());
                Assert.assertFalse(true);
            } catch (FileNotFoundException e1) {
                LOGGER.error(e1.getMessage());
                Assert.assertFalse(true);
            }

            Ast ast = dt.getAst();
            Assert.assertNotNull(ast);
            Assert.assertTrue(ast.getNodes().size() > 1);
        });


    }


}
