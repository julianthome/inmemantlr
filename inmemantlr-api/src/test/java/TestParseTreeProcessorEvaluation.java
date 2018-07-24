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
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;
import org.snt.inmemantlr.tree.ParseTreeProcessor;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


public class TestParseTreeProcessorEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestParseTreeProcessorEvaluation.class);

    String sgrammarcontent = "";

    @Test
    public void testInterpreter() throws IOException {

        try (InputStream sgrammar = getClass().getClassLoader()
                .getResourceAsStream("inmemantlr/Ops.g4")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        }

        GenericParser gp = new GenericParser(sgrammarcontent);
        DefaultTreeListener t = new DefaultTreeListener();

        gp.setListener(t);


        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);


        // this example shows you how one could use inmemantlr for incremental parsing
        try {
            ParseTree parseTree;
            gp.parse("3+100");
            parseTree = t.getParseTree();

            // Process the tree bottom up
            ParseTreeProcessor<String, String> processor = new ParseTreeProcessor<String, String>(parseTree) {
                @Override
                public String getResult() {
                    return smap.get(this.parseTree.getRoot());
                }

                @Override
                protected void initialize() {
                    this.parseTree.getNodes().forEach(n -> smap.put(n, n.getLabel()));
                }

                @Override
                protected void process(ParseTreeNode n) {
                    LOGGER.debug("id " + n.getId());
                    if (n.getRule().equals("expression")) {
                        int n0 = Integer.parseInt(smap.get(n.getChild(0)));
                        int n1 = Integer.parseInt(smap.get(n.getChild(2)));
                        int result = 0;
                        switch (smap.get(n.getChild(1))) {
                            case "+":
                                result = n0 + n1;
                                break;
                            case "-":
                                result = n0 - n1;
                                break;
                        }
                        smap.put(n, String.valueOf(result));
                    } else
                        simpleProp(n);
                }
            };


            assertDoesNotThrow((Executable) processor::process);
            assertEquals(7, parseTree.getNodes().size());
            assertEquals("103", processor.getResult());
        } catch (IllegalWorkflowException | ParsingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}