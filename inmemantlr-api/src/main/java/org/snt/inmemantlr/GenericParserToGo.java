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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * a simpler generic parser representation to get going quickly
 */
public class GenericParserToGo {

    private GenericParser gp;
    private DefaultTreeListener dl;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParserToGo.class);

    /**
     * constructor
     * @param grammar grammar(s) content
     */
    public GenericParserToGo(String ... grammar) {

        gp = new GenericParser(grammar);

        try {
            gp.compile();
        } catch (CompilationException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        dl = new DefaultTreeListener(true);
        gp.setListener(dl);

    }

    /**
     * constructor
     * @param grammar ANTLR file(s)
     */
    public GenericParserToGo(File ... grammar) {
        try {
            gp = new GenericParser(grammar);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        try {
            gp.compile();
        } catch (CompilationException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        dl = new DefaultTreeListener(true);
        gp.setListener(dl);
    }


    /**
     * parse a string
     * @param s a string to parse
     * @param rule parsing rule to start
     * @return parse tree of s
     */
    public ParseTree parse(String s, String rule) {
        try {
            gp.parse(s, rule, GenericParser.CaseSensitiveType.NONE);
            return dl.getParseTree();
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        } catch (ParsingException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * parse a file
     * @param f a file to parse
     * @param rule parsing rule to start
     * @return parse tree of f'c content
     */
    public ParseTree parse(File f, String rule) {
        try {
            gp.parse(f, rule, GenericParser.CaseSensitiveType.NONE);
            return dl.getParseTree();
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        } catch (ParsingException e) {
            LOGGER.error(e.getMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return null;
    }

}
