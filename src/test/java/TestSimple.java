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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class TestSimple {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSimple.class);

    String sgrammarcontent = "";

    @Test
    public void testInterpreter() throws IOException {

        try (InputStream sgrammar = getClass().getClassLoader().getResourceAsStream("Simple.g4")) {
            sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
        }

        GenericParser gp = new GenericParser(sgrammarcontent, null);
        DefaultTreeListener t = new DefaultTreeListener();

        gp.setListener(t);
        gp.compile();

        // this example shows you how one could use inmemantlr for incremental parsing
        try {
            Ast ast;
            gp.parse("PRINT a+b");
            ast = t.getAst();
            LOGGER.debug(ast.toDot());
            assertEquals(ast.getNodes().size(), 6);
            gp.parse("PRINT \"test\"");
            ast = t.getAst();
            LOGGER.debug(ast.toDot());
            assertEquals(ast.getNodes().size(), 4);
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}