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

import org.junit.Before;
import org.junit.Test;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.DeserializationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.listener.DefaultListener;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class TestGenericParser {

    static File grammar = null;
    static File sfile = null;

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        grammar = new File(classLoader.getResource("inmemantlr/Java.g4").getFile());
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

        assertNotNull(gp);

        // Incorrect workflows
        boolean thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException e) {
            thrown = true;
        }

        assertTrue(thrown);
        gp.compile();

        // Incorrect workflows
        thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException e) {
            thrown = true;
        }

        gp.compile();
        thrown = false;
        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException e) {
            thrown = true;
        }

        assertFalse(thrown);
        thrown = false;
        assertNotSame(gp.getListener(), null);

        // Correct workflow
        gp.setListener(new DefaultListener());
        assertNotNull(gp.getListener());
        gp.compile();

        try {
            gp.parse(sfile);
        } catch (IllegalWorkflowException | FileNotFoundException e) {
            thrown = true;
        }

        assertFalse(thrown);
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
        assertTrue(thrown);
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
        assertTrue(thrown);
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
        assertTrue(thrown);
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
        assertTrue(thrown);
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
        assertTrue(thrown);
    }

}
