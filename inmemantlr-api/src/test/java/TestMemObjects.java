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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.DeserializationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.SerializationException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.memobjects.MemoryByteCode;
import org.snt.inmemantlr.memobjects.MemoryTuple;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.*;

public class TestMemObjects {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMemObjects.class);

    static File grammar = null;
    static File sfile = null;

    private String fname = "temp.gout";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        grammar = new File(classLoader.getResource("inmemantlr/Java.g4").getFile());
        sfile = new File(classLoader.getResource("inmemantlr/HelloWorld.java").getFile());
    }

    @Test
    public void testAntlrObjectAccess() {
        GenericParser gp = null;
        try {
            gp = new GenericParser(grammar);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotNull(gp);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);

        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

        assertTrue(s != null && !s.isEmpty());

        MemoryTupleSet set = gp.getAllCompiledObjects();

        assertTrue(set != null && set.size() == 4);

        for (MemoryTuple tup : set) {
            LOGGER.debug("tuple name {}", tup.getClassName());
            // for printing the source code
            LOGGER.debug("source {}", tup.getSource().getClassName());
            // for printing the byte code
            for (MemoryByteCode mc : tup.getByteCodeObjects()) {
                Objects.requireNonNull(mc, "MemoryByteCode must not be null");
                LOGGER.debug("bc name: {}", mc.getClassName());

                if (!mc.isInnerClass()) {
                    mc.getClassName().equals(tup.getSource().getClassName());
                } else {
                    mc.getClassName().startsWith(tup.getSource().getClassName());
                }
            }
        }
    }

    @Test
    public void testStoreAndLoad() {
        GenericParser gp = null;
        try {
            gp = new GenericParser(grammar);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotNull(gp);


        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        assertTrue(compile);

        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

        assertTrue(s != null && !s.isEmpty());

        File file = null;

        try {
            file = folder.newFile(fname);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        try {
            gp.parse(s);
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        try {
            gp.store(file.getAbsolutePath(), true);
        } catch (SerializationException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        GenericParser cgp = null;

        try {
            cgp = GenericParser.load(file.getAbsolutePath());
        } catch (DeserializationException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        try {
            cgp.parse(s);
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        DefaultTreeListener dlist = new DefaultTreeListener();
        cgp.setListener(dlist);

        try {
            cgp.parse(s);
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            assertFalse(true);
        }

        assertTrue(dlist.getAst() != null);

        LOGGER.debug(dlist.getAst().toDot());
    }
}
