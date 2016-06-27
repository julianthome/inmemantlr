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

package org.snt.helix;

import org.antlr.v4.runtime.ParserRuleContext;
import org.snt.inmemantlr.DefaultListener;
import org.snt.inmemantlr.GenericParser;
import org.junit.Test;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestGenericParser {

    @Test
    public void testParser() {


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

        assertTrue(bytes != null);

        String s = new String(bytes);

        assertTrue(s.length() > 0);


        /**
         * Incorrect workflows
         */

        boolean thrown = false;
        try {
            gp.parse(s);
        } catch (IllegalWorkflowException e) {
            thrown = true;
        }

        gp.compile();

        thrown = false;

        ParserRuleContext ctx = null;

        try {
            ctx = gp.parse(s);
        } catch (IllegalWorkflowException e) {
            thrown = true;
        }

        assertTrue(thrown);

        thrown = false;

        /**
         * Correct workflow
         */
        gp.setListener(new DefaultListener());
        gp.compile();

        try {
            ctx = gp.parse(s);
        } catch (IllegalWorkflowException e) {
            thrown = true;
        }

        assertFalse(thrown);
        assertTrue(ctx != null);

    }

}
