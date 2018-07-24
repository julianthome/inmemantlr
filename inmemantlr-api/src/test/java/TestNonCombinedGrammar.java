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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;

import java.io.File;
import java.io.IOException;


public class TestNonCombinedGrammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestNonCombinedGrammar.class);

    @Test
    public void testParserLexer() throws IOException {
        LOGGER.debug("Test multi file parsing");

        File files[] = {
                new File(getClass().getClassLoader().getResource
                        ("inmemantlr/MySQLLexer.g4").getFile()),
                new File(getClass().getClassLoader().getResource
                        ("inmemantlr/MySQLParser.g4").getFile())
        };

        GenericParser gp = new GenericParser(files);
        DefaultTreeListener t = new DefaultTreeListener();
        gp.setListener(t);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        Assertions.assertTrue(compile);

        try {
            ParseTree parseTree;
            gp.parse("select a from b;");
            parseTree = t.getParseTree();
            Assertions.assertEquals(13, parseTree.getNodes().size());
            LOGGER.debug(parseTree.toDot());
        } catch (IllegalWorkflowException | ParsingException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}