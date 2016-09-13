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
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.util.Set;

public class TestTreeGeneration {

    static File grammar = null;
    static File sfile = null;

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        grammar = new File(classLoader.getResource("Java.g4").getFile());
        sfile = new File(classLoader.getResource("HelloWorld.java").getFile());
    }

    @Test
    public void testGeneration() {

        GenericParser gp = new GenericParser(grammar, "Java");
        gp.compile();
        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

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

        // create copy
        Ast cast = new Ast(ast);

        Assert.assertTrue(ast != null);
        Assert.assertEquals(ast.getNodes().size(),cast.getNodes().size());

        Set<Ast> asts = ast.getSubtrees(n -> n.getRule().equals("expression"));

        assert(asts.size() == 5);

        for(Ast a : asts) {
            Assert.assertTrue(ast.hasSubtree(a));
            Assert.assertFalse(ast.getSubtree(a) == null);
        }

        int sizeBefore = ast.getNodes().size();

        Ast first = asts.iterator().next();

        ast.removeSubtree(first);

        Assert.assertTrue((ast.getNodes().size() + first.getNodes().size()) == sizeBefore);

        Ast repl = new Ast("replacement", "replacement");

        cast.replaceSubtree(first, repl);

        Assert.assertTrue(cast.getNodes().size() == ast.getNodes().size() + 1);
        Assert.assertTrue(cast.getDominatingSubtrees( n -> n.getRule().equals("classBody")).size() == 1);
        Assert.assertTrue(cast.toDot() != null && cast.toDot().length() > 0);

        AstNode root = cast.getRoot();

        Assert.assertTrue(root.hasChildren() == true);
        Assert.assertFalse(root.hasParent());

        for(AstNode n : cast.getNodes()) {
            Assert.assertTrue(n.getLabel() != null);
            Assert.assertTrue(n.getRule() != null);
            for(int i = 0; i < n.getChildren().size(); i++) {
                if(i == 0)
                    Assert.assertTrue(n.getChild(i).equals(n.getFirstChild()));
                if(i == n.getChildren().size() - 1)
                    Assert.assertTrue(n.getChild(i).equals(n.getLastChild()));
            }
        }

        for(AstNode c : cast.getLeafs()) {
            Assert.assertTrue(c.hasParent());
            Assert.assertFalse(c.hasChildren());
            Assert.assertTrue(c.isLeaf());
            Assert.assertFalse(c.equals(null));
            Assert.assertFalse(c.equals(null));
            Assert.assertEquals(c.getLastChild(),null);
            Assert.assertEquals(c.getFirstChild(),null);
        }


        AstNode croot = cast.getRoot();
        croot.setParent(ast.getRoot());
        Assert.assertTrue(croot.getParent().equals(ast.getRoot()));
    }

}
