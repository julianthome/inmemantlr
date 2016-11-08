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

import org.junit.Assert;
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

import static org.junit.Assert.*;

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
        GenericParser gp = new GenericParser(grammar, null);
        gp.compile();
        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

        assertTrue(s != null && !s.isEmpty());

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        try {
            gp.parse(s);
        } catch (IllegalWorkflowException e) {
            assertTrue(false);
        }

        Ast ast = dlist.getAst();

        // create copy
        Ast cast = new Ast(ast);

        assertTrue(ast != null);
        Assert.assertEquals(ast.getNodes().size(), cast.getNodes().size());

        Set<Ast> asts = ast.getSubtrees(n -> "expression".equals(n.getRule()));

        assert asts.size() == 5;

        for (Ast a : asts) {
            assertTrue(ast.hasSubtree(a));
            assertFalse(ast.getSubtree(a) == null);
        }

        int sizeBefore = ast.getNodes().size();

        Ast first = asts.iterator().next();

        ast.removeSubtree(first);

        assertTrue(ast.getNodes().size() + first.getNodes().size() == sizeBefore);

        Ast repl = new Ast("replacement", "replacement");

        cast.replaceSubtree(first, repl);

        assertTrue(cast.getNodes().size() == ast.getNodes().size() + 1);
        assertTrue(cast.getDominatingSubtrees(n -> "classBody".equals(n.getRule())).size() == 1);
        assertTrue(cast.toDot() != null && !cast.toDot().isEmpty());

        AstNode root = cast.getRoot();

        assertTrue(root.hasChildren());
        assertFalse(root.hasParent());

        for (AstNode n : cast.getNodes()) {
            assertTrue(n.getLabel() != null);
            assertTrue(n.getRule() != null);
            for (int i = 0; i < n.getChildren().size(); i++) {
                if (i == 0)
                    assertTrue(n.getChild(i).equals(n.getFirstChild()));
                if (i == n.getChildren().size() - 1)
                    assertTrue(n.getChild(i).equals(n.getLastChild()));
            }
        }

        for (AstNode c : cast.getLeafs()) {
            assertTrue(c.hasParent());
            assertFalse(c.hasChildren());
            assertTrue(c.isLeaf());
            assertFalse(c.equals(null));
            assertFalse(c.equals(null));
            assertNull(c.getLastChild());
            assertNull(c.getFirstChild());
        }

        AstNode croot = cast.getRoot();
        croot.setParent(ast.getRoot());
        assertTrue(croot.getParent().equals(ast.getRoot()));
    }
}
