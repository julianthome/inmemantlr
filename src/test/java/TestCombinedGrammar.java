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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.DefaultTreeListener;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.tree.Ast;

import java.io.File;
import java.io.IOException;

public class TestCombinedGrammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCombinedGrammar.class);

    @Test
    public void testParserLexer() throws IOException {


        LOGGER.debug("Test multi file parsing");

        File files [] = {
                new File(getClass().getClassLoader().getResource
                        ("MySQLLexer.g4").getFile()),
                new File(getClass().getClassLoader().getResource
                        ("MySQLParser.g4").getFile())
        };


        GenericParser gp = new GenericParser(files);
        DefaultTreeListener t = new DefaultTreeListener();
        gp.setListener(t);

        if(!gp.compile()) {
            LOGGER.error("Compilation error");
            return;
        }

        try {
            Ast ast;
            gp.parse("select a from b;");
            ast = t.getAst();
            Assert.assertEquals(ast.getNodes().size(), 13);
            LOGGER.debug(ast.toDot());
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}