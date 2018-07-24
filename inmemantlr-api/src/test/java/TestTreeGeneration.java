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
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class TestTreeGeneration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTreeGeneration.class);

    static File grammar = null;
    static File sfile = null;



    static {
        ClassLoader classLoader = TestTreeGeneration.class
                .getClassLoader();
        grammar = new File(classLoader.getResource("inmemantlr/Java.g4")
                .getFile());
        sfile = new File(classLoader.getResource("inmemantlr/HelloWorld.java").getFile());
    }

    @Test
    public void testGeneration() {

        assertNotNull(grammar);

        GenericParser gp = assertDoesNotThrow(() -> new GenericParser(grammar));

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

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        assertDoesNotThrow(() -> {
            gp.parse(s);
        });

        ParseTree parseTree = dlist.getParseTree();

        LOGGER.debug(parseTree.toDot());

        // create copy
        ParseTree cast = new ParseTree(parseTree);

        assertNotNull(parseTree);
        assertEquals(parseTree.getNodes().size(), cast.getNodes().size());

        Set<ParseTree> parseTrees = parseTree.getSubtrees(n -> "expression".equals(n.getRule()));

        assertEquals(5, parseTrees.size());

        for (ParseTree a : parseTrees) {
            assertTrue(parseTree.hasSubtree(a));
            assertNotNull(parseTree.getSubtree(a));
        }

        int sizeBefore = parseTree.getNodes().size();

        ParseTree first = parseTrees.iterator().next();

        parseTree.removeSubtree(first);

        assertEquals(parseTree.getNodes().size() + first.getNodes().size(), sizeBefore);

        ParseTree repl = new ParseTree("replacement", "replacement");

        cast.replaceSubtree(first, repl);

        assertEquals(cast.getNodes().size(), parseTree.getNodes().size() + 1);
        assertEquals(1, cast.getDominatingSubtrees(n -> "classBody".equals(n.getRule())).size());
        assertTrue(cast.toDot() != null && !cast.toDot().isEmpty());

        ParseTreeNode root = cast.getRoot();

        assertTrue(root.hasChildren());
        assertFalse(root.hasParent());

        for (ParseTreeNode n : cast.getNodes()) {
            assertNotNull(n.getLabel());
            assertNotNull(n.getRule());
            for (int i = 0; i < n.getChildren().size(); i++) {
                if (i == 0)
                    assertEquals(n.getChild(i), n.getFirstChild());
                if (i == n.getChildren().size() - 1)
                    assertEquals(n.getChild(i), n.getLastChild());
            }
        }

        for (ParseTreeNode c : cast.getLeafs()) {
            assertTrue(c.hasParent());
            assertFalse(c.hasChildren());
            assertTrue(c.isLeaf());
            assertNull(c.getLastChild());
            assertNull(c.getFirstChild());
        }

        ParseTreeNode croot = cast.getRoot();
        croot.setParent(parseTree.getRoot());
        assertEquals(croot.getParent(), parseTree.getRoot());
    }
}
