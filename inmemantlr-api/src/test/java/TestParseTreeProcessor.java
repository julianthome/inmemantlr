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


import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParseTreeProcessorException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;
import org.snt.inmemantlr.tree.ParseTreeProcessor;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


public class TestParseTreeProcessor {

    private static String sgrammarcontent = "";
    private static String s = "";

    static {
        ClassLoader classLoader = TestParseTreeProcessor.class.getClassLoader();

        assertDoesNotThrow(() -> {
            try (InputStream sgrammar = classLoader.getResourceAsStream("inmemantlr/Java.g4");
                 InputStream sfile = classLoader.getResourceAsStream("inmemantlr/HelloWorld.java")) {
                sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
                s = FileUtils.getStringFromStream(sfile);
            }
        });
    }

    @Test
    public void testProcessor() {
        GenericParser gp = new GenericParser(sgrammarcontent);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);
        assertTrue(s != null && !s.isEmpty());

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        try {
            gp.parse(s);
        } catch (IllegalWorkflowException | ParsingException e) {
            fail();
        }

        ParseTree parseTree = dlist.getParseTree();

        // Process the tree bottom up
        ParseTreeProcessor<String, String> processor = new ParseTreeProcessor<String, String>(parseTree) {
            int cnt = 0;

            @Override
            public String getResult() {
                return String.valueOf(cnt);
            }

            @Override
            protected void initialize() {
                for (ParseTreeNode n : this.parseTree.getNodes()) {
                    smap.put(n, "");
                }
            }

            @Override
            protected void process(ParseTreeNode n) {
                cnt++;
                simpleProp(n);
                assertNotNull(getElement(n));
            }
        };

        try {
            processor.process();
        } catch (ParseTreeProcessorException e) {
            fail();
        }
        assertNotNull(processor.debug());
    }
}
