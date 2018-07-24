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
import org.snt.inmemantlr.exceptions.*;
import org.snt.inmemantlr.listener.DefaultListener;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.fail;



public class TestGenericParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestGenericParser.class);

    static File grammar = null;
    static File fgrammar = null;
    static File sfile = null;

    static {
        ClassLoader classLoader = TestGenericParser.class.getClassLoader();
        grammar = new File(classLoader.getResource("inmemantlr/Java.g4").getFile());
        fgrammar = new File(classLoader.getResource("inmemantlr/Fail.g4")
                .getFile());
        sfile = new File(classLoader.getResource("inmemantlr/HelloWorld.java").getFile());
    }

    @Test
    public void testParser() {
        GenericParser gp = null;
        try {
            gp = new GenericParser(grammar);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertNotNull(gp);

        // Incorrect workflows
        boolean thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException |
                ParsingException e) {
            thrown = true;
        }

        Assertions.assertTrue(thrown);


        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        Assertions.assertTrue(compile);

        // Incorrect workflows
        thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException | ParsingException e) {
            thrown = true;
        }

        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {

            if(e instanceof RedundantCompilationException)
                compile = true;
            else
                compile = false;
        }

        Assertions.assertTrue(compile);


        thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException | ParsingException e) {
            thrown = true;
        }

        Assertions.assertFalse(thrown);
        thrown = false;
        Assertions.assertNotSame(null, gp.getListener());

        // Correct workflow
        gp.setListener(new DefaultListener());
        Assertions.assertNotNull(gp.getListener());


        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {

            if(e instanceof RedundantCompilationException)
                compile = true;
            else
                compile = false;
        }

        Assertions.assertTrue(compile);

        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException |
                ParsingException e) {
            thrown = true;
        }

        Assertions.assertFalse(thrown);
    }

    @Test
    public void testDeserializationException() {
        boolean thrown = false;
        // Undeclared exception!
        try {
            GenericParser.load("");
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException | DeserializationException e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void testWrongParams1Exception() {
        boolean thrown = false;
        GenericParser genericParser0 = null;
        try {
            genericParser0 = new GenericParser((File) null);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException | FileNotFoundException e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void testWrongParams2Exception() {
        boolean thrown = false;
        GenericParser genericParser0 = null;
        try {
            genericParser0 = new GenericParser("xyz");
            fail("Expecting exception: Error");
        } catch (NullPointerException | Error e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void testIndepenedentInstanceException() {
        boolean thrown = false;
        try {
            GenericParser.independentInstance(null, "Jayi,3c29V@fo?BF@_");
            fail("Expecting exception: Error");
        } catch (NullPointerException | Error e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void testInstanceException() {
        boolean thrown = false;
        try {
            GenericParser.instance(null, "Jayi,3c29V@fo?BF@_");
            fail("Expecting exception: Error");
        } catch (NullPointerException | Error e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void testCompilationError() {
        GenericParser gp = null;
        try {
            gp = new GenericParser(fgrammar);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertNotNull(gp);

        // Incorrect workflows
        boolean thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException | ParsingException e) {
            thrown = true;
        }

        Assertions.assertTrue(thrown);

        boolean compile = true;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            if(e instanceof CompilationErrorException) {
                compile = false;
            }
        }

        Assertions.assertFalse(compile);

    }

}
