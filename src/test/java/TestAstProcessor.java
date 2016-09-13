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
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.tree.AstNode;
import org.snt.inmemantlr.tree.AstProcessor;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.InputStream;


public class TestAstProcessor {

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
    public void testProcessor() {

        GenericParser gp = new GenericParser(sgrammarcontent, "Java");
        gp.compile();

        Assert.assertTrue(s != null && s.length() > 0);

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        ParserRuleContext ctx = null;
        try {
            ctx = gp.parse(s);
        } catch (IllegalWorkflowException e) {
            Assert.assertTrue(false);
        }

        Ast ast = dlist.getAst();


        // Process the tree botton up
        AstProcessor<String,String> processor = new AstProcessor<String, String>(ast) {
            int cnt = 0;
            @Override
            public String getResult() {
                return String.valueOf(cnt);
            }

            @Override
            protected void initialize() {
                for(AstNode n : this.ast.getNodes()) {
                    this.smap.put(n, "");
                }
            }
            @Override
            protected void process(AstNode n) {
                cnt ++;
                simpleProp(n);
                Assert.assertTrue(getElement(n) != null);
            }
        };

        processor.process();
        Assert.assertTrue(processor.debug() != null);
        processor.getResult().equals(String.valueOf(ast.getNodes()));

    }

}
