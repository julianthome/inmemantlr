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
import org.junit.Test;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.snt.inmemantlr.utils.FileUtils.getStringFromStream;

public class TestDoubleParse {

    String sgrammarcontent = "";
    String s1 = "", s2 = "";

    @Before
    public void init() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream sgrammar = classLoader.getResourceAsStream("Java.g4");
             InputStream sfile1 = classLoader.getResourceAsStream("HelloWorld.java");
             InputStream sfile2 = classLoader.getResourceAsStream("HelloUniverse.java")) {
            sgrammarcontent = getStringFromStream(sgrammar);
            s1 = getStringFromStream(sfile1);
            s2 = getStringFromStream(sfile2);
        }
    }

    @Test
    public void testProcessor() {
        GenericParser gp1 = new GenericParser(sgrammarcontent, "Java", null);
        gp1.compile();
        GenericParser gp2 = new GenericParser(sgrammarcontent, "Java", null);
        gp2.compile();

        DefaultTreeListener l1 = new DefaultTreeListener();
        DefaultTreeListener l2 = new DefaultTreeListener();

        assertFalse(l1.toString() == null);
        assertEquals(l1.toString(), "");

        gp1.setListener(l1);
        gp2.setListener(l2);

        try {
            gp1.parse(s1);
            gp2.parse(s2);
        } catch (IllegalWorkflowException e) {
            e.printStackTrace();
        }

        Ast out1 = l1.getAst();
        Ast out2 = l2.getAst();
        assertFalse(out1.equals(out2));
        assertTrue(out1.equals(out1));
    }
}
