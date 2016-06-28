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


package org.snt.inmenantlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.junit.Test;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;

import java.io.*;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;


public class TestTreeGeneration {

    @Test
    public void testGeneration() {

        GenericParser gp = new GenericParser("src/test/ressources/Java.g4", "Java");
        gp.compile();

        byte[] bytes = null;
        try {
            RandomAccessFile f = new RandomAccessFile("src/test/ressources/HelloWorld.java", "r");
            bytes = new byte[(int)f.length()];
            f.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);
        gp.compile();

        String s = new String(bytes);

        ParserRuleContext ctx = null;
        try {
            ctx = gp.parse(s);
        } catch (IllegalWorkflowException e) {}


        /**Ast ast = dlist.getAst();

        assertTrue(ast != null);

        // get all expression subtrees
        Set<Ast> asts = ast.getSubtrees(n -> n.getRule().equals("expression"));

        assert(asts.size() == 5);

        for(Ast a : asts) {
            assertTrue(ast.hasSubtree(a));
        }

        int sizeBefore = ast.getNodes().size();

        Ast first = asts.iterator().next();

        ast.removeSubtree(first);

        assertTrue((ast.getNodes().size() + first.getNodes().size()) == sizeBefore);

        // print changed tree

        Ast replacement = new Ast("test", "test");

        Set<Ast> asts2 = ast.getSubtrees(n -> n.getId() == 2);

        assertTrue(asts2.size() == 1);

        Ast old = asts2.iterator().next();

        Ast oldCp = new Ast(asts2.iterator().next());

        assertTrue(old.equals(oldCp));

        assertTrue(ast.replaceSubtree(old, replacement));**/

    }

}
