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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.memobjects.MemoryByteCode;
import org.snt.inmemantlr.memobjects.MemoryTuple;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemObjects {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMemObjects.class);

    static File grammar = null;
    static File sfile = null;

    private String fname = "temp.gout";


    static {
        ClassLoader classLoader = TestMemObjects.class.getClassLoader();
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

                if (mc.isInnerClass()) {
                    assertTrue(mc.getClassName().startsWith(tup.getSource().getClassName()));
                } else {
                    assertEquals(tup.getSource().getClassName(), mc.getClassName());
                }
            }
        }
    }

    @Test
    public void testStoreAndLoad() throws CompilationException {
        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(grammar));

        assertNotNull(gp);

        File file = assertDoesNotThrow(() -> File.createTempFile("temp", Long.toString(System.nanoTime())));

        file.mkdir();

        gp.compile();

        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

        assertTrue(s != null && !s.isEmpty());


        assertDoesNotThrow(() -> {
            gp.parse(s);
        });

        assertDoesNotThrow(() -> {
            gp.store(file.getAbsolutePath(), true);
        });

        GenericParser cgp = assertDoesNotThrow(() -> GenericParser.load(file.getAbsolutePath()));


        assertDoesNotThrow(() -> {
            cgp.parse(s);
        });

        DefaultTreeListener dlist = new DefaultTreeListener();
        cgp.setListener(dlist);

        assertDoesNotThrow(() -> {
            cgp.parse(s);
        });

        assertNotNull(dlist.getParseTree());

        LOGGER.debug(dlist.getParseTree().toDot());
    }
}
