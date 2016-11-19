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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestExternalGrammars {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExternalGrammars.class);


    // @TODO: add distinct test cases for test modules
    private static String [] exclude = {
        "antlr3", "antlr4", "aspectj", "csharp", "ecmascript",
            "objc", "oncrpc", "php" , "sqlite" , "sparql",
            "stringtemplate", "swift", "swift-fin", "xml",
            "pgn", "ucb-logo" , "agc"
    };

    static File grammar = null;

    private Set<String> blacklist = new HashSet(
            Arrays.asList(exclude)
    );


    private static class Subject {
        public String name;
        public File [] g4;
        public File [] examples;


        public boolean hasExamples() {
            return examples != null;
        }

        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();

            sb.append("name:");
            sb.append(name);
            sb.append("\n");
            sb.append("g4 files:\n");
            for (File file : g4) {
                sb.append(file.getAbsolutePath());
                sb.append("\n");
            }
            if(hasExamples()) {
                sb.append("examples:\n");
                for (File file : examples) {
                    sb.append(file.getAbsolutePath());
                    sb.append("\n");
                }
            }
            sb.append("\n");

            return sb.toString();
        }
    }


    private Subject [] subjects = null;

    @Before
    public void init() {

        ClassLoader classLoader = getClass().getClassLoader();

        if(classLoader.getResource("grammars-v4") == null)
            return;

        grammar = new File(classLoader.getResource("grammars-v4")
                .getFile());


        File [] files = grammar.listFiles(pathname -> pathname.isDirectory() &&
                !blacklist.contains(pathname.getName()));

        Arrays.stream(files);

        subjects = new Subject[files.length];

        int idx = 0;

        for(File f : files) {
            assertTrue(f.isDirectory());
            Subject subject = new Subject();
            subject.name = f.getName();
            subject.g4 = f.listFiles(pathname -> pathname.getName().endsWith
                    (".g4"));
            File examples = new File(f.getAbsolutePath() + "/examples");
            subject.examples = examples.listFiles(pathname -> !pathname
                    .isDirectory());
            subjects[idx++] = subject;
        }

        assertEquals(idx, subjects.length);
    }

    private void testSubject(Subject s) {

        LOGGER.debug("test {}", s.name);
        GenericParser gp = null;
        try {
            LOGGER.debug("create parser with {} files", s.g4.length);
            gp = new GenericParser(s.g4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(gp.compile());
        LOGGER.debug("successfully compiled grammar");

        DefaultTreeListener dt = new DefaultTreeListener();
        gp.setListener(dt);

        if(s.hasExamples()) {
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
        for (Subject s : subjects) {
            //LOGGER.debug(s.toString());
            if(s.hasExamples())
                testSubject(s);
        }
    }
}
