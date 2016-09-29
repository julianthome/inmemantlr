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
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.StringCodeGenPipeline;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class TestStringCodeGenPipeline {

    String sgrammarcontent = "";
    String s = "";

    @Before
    public void init() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream sgrammar = classLoader.getResourceAsStream("Java.g4");
             InputStream sfile = classLoader.getResourceAsStream("HelloWorld.java")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
            s = FileUtils.getStringFromStream(sfile);
        }
    }

    @Test
    public void testCodeGenPipeline() {
        GenericParser gp = new GenericParser(sgrammarcontent, "Java", null);
        gp.compile();

        assertTrue(s != null && !s.isEmpty());
        assertNotNull(gp.getGrammar());

        StringCodeGenPipeline sg = new StringCodeGenPipeline(gp.getGrammar(), "Java");

        assertTrue("JavaVisitor".equals(sg.getVisitorName()));
        assertTrue("JavaLexer".equals(sg.getLexerName()));
        assertTrue("JavaListener".equals(sg.getListenerName()));
        assertTrue("JavaBaseListener".equals(sg.getBaseListenerName()));
        assertTrue("JavaBaseVisitor".equals(sg.getBaseVisitorName()));
        assertTrue("JavaParser".equals(sg.getParserName()));

        assertNull(sg.getVisitor());
        assertNull(sg.getLexer());
        assertNull(sg.getListener());
        assertNull(sg.getBaseListener());
        assertNull(sg.getBaseVisitor());
        assertNull(sg.getParser());

        assertFalse(sg.hasVisitor());
        assertFalse(sg.hasLexer());
        assertFalse(sg.hasListener());
        assertFalse(sg.hasBaseListener());
        assertFalse(sg.hasBaseVisitor());
        assertFalse(sg.hasParser());

        sg.process();

        assertNotNull(sg.getLexer());
        assertNotNull(sg.getListener());
        assertNotNull(sg.getBaseListener());
        assertNotNull(sg.getParser());

        assertTrue(sg.hasBaseListener());
        assertTrue(sg.hasListener());
        assertTrue(sg.hasBaseListener());
        assertTrue(sg.hasParser());

        assertTrue(sg.getG().equals(gp.getGrammar()));
    }
}
