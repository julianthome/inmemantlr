/*
* Inmemantlr - In memory compiler for Antlr 4
*
* Copyright 2016, Julian Thomé <julian.thome@uni.lu>
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence. You may
* obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.DeserializationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.SerializationException;
import org.snt.inmemantlr.memobjects.MemoryByteCode;
import org.snt.inmemantlr.memobjects.MemoryTuple;
import org.snt.inmemantlr.memobjects.MemoryTupleSet;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        grammar = new File(classLoader.getResource("Java.g4").getFile());
        sfile = new File(classLoader.getResource("HelloWorld.java").getFile());
    }

    @Test
    public void testAntlrObjectAccess() {
        GenericParser gp = new GenericParser(grammar, null);

        assertTrue(gp.compile());

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
                assert mc != null;
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
        GenericParser gp = new GenericParser(grammar, null);
        gp.compile();
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
