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
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;


public class TestGenericParser {

    static File grammar = null;
    static File sfile = null;

    @Before
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        grammar = new File(classLoader.getResource("Java.g4").getFile());
        sfile = new File(classLoader.getResource("HelloWorld.java").getFile());
    }

    @Test
    public void testParser() {

        GenericParser gp = new GenericParser(grammar, "Java");
        gp.compile();
        String s = FileUtils.loadFileContent(sfile.getAbsolutePath());

        Assert.assertTrue(s != null && s.length() > 0);

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

        Assert.assertTrue(thrown);

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

        Assert.assertFalse(thrown);
    }

}
