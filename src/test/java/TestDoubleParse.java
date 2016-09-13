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
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Before;
import org.junit.Test;
import org.snt.inmemantlr.DefaultListener;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.tree.AstNode;
import org.snt.inmemantlr.tree.AstProcessor;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.InputStream;


public class TestDoubleParse {

    static InputStream sgrammar = null;
    static InputStream sfile1 = null;
    static InputStream sfile2 = null;

    String sgrammarcontent = "";
    String s1 = "", s2 = "";

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        sgrammar = classLoader.getResourceAsStream("Java.g4");
        sfile1 = classLoader.getResourceAsStream("HelloWorld.java");
        sfile2 = classLoader.getResourceAsStream("HelloUniverse.java");

        sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        s1 = FileUtils.getStringFromStream(sfile1);
        s2 = FileUtils.getStringFromStream(sfile2);
    }

    @Test
    public void testProcessor() {

        GenericParser gp1 = new GenericParser(sgrammarcontent, "Java");
        gp1.compile();
        GenericParser gp2 = new GenericParser(sgrammarcontent, "Java");
        gp2.compile();

        DefaultTreeListener l1 = new DefaultTreeListener();
        DefaultTreeListener l2 = new DefaultTreeListener();

        Assert.assertFalse(l1.toString() == null);
        Assert.assertEquals(l1.toString(), "");

        ParserRuleContext ctx = null;
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
        Assert.assertFalse(out1.equals(out2));
        Assert.assertTrue(out1.equals(out1));
    }

}
