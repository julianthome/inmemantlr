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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.StringCodeGenPipeline;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.InputStream;


public class TestStringCodeGenPipeline {

    static InputStream sgrammar = null;
    static InputStream sfile = null;

    String sgrammarcontent = "";
    String s = "";

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        sgrammar = classLoader.getResourceAsStream("Java.g4");
        sfile = classLoader.getResourceAsStream("HelloWorld.java");

        sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        s = FileUtils.getStringFromStream(sfile);
    }

    @Test
    public void testCodeGenPipeline() {

        GenericParser gp = new GenericParser(sgrammarcontent, "Java", null);
        gp.compile();

        Assert.assertTrue(s != null && s.length() > 0);
        Assert.assertTrue(gp.getGrammar() != null);

        StringCodeGenPipeline sg = new StringCodeGenPipeline(gp.getGrammar(), "Java");

        Assert.assertTrue(sg.getVisitorName().equals("JavaVisitor"));
        Assert.assertTrue(sg.getLexerName().equals("JavaLexer"));
        Assert.assertTrue(sg.getListenerName().equals("JavaListener"));
        Assert.assertTrue(sg.getBaseListenerName().equals("JavaBaseListener"));
        Assert.assertTrue(sg.getBaseVisitorName().equals("JavaBaseVisitor"));
        Assert.assertTrue(sg.getParserName().equals("JavaParser"));

        Assert.assertTrue(sg.getVisitor() == null);
        Assert.assertTrue(sg.getLexer() == null);
        Assert.assertTrue(sg.getListener() == null);
        Assert.assertTrue(sg.getBaseListener() == null);
        Assert.assertTrue(sg.getBaseVisitor() == null);
        Assert.assertTrue(sg.getParser() == null);

        Assert.assertFalse(sg.hasVisitor());
        Assert.assertFalse(sg.hasLexer());
        Assert.assertFalse(sg.hasListener());
        Assert.assertFalse(sg.hasBaseListener());
        Assert.assertFalse(sg.hasBaseVisitor());
        Assert.assertFalse(sg.hasParser());


        sg.process();

        Assert.assertFalse(sg.getLexer() == null);
        Assert.assertFalse(sg.getListener() == null);
        Assert.assertFalse(sg.getBaseListener() == null);
        Assert.assertFalse(sg.getParser() == null);

        Assert.assertTrue(sg.hasBaseListener());
        Assert.assertTrue(sg.hasListener());
        Assert.assertTrue(sg.hasBaseListener());
        Assert.assertTrue(sg.hasParser());

        Assert.assertTrue(sg.getG().equals(gp.getGrammar()));

    }
}
