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

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParser;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.utils.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestProcessors {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessors.class);

    private static String sgrammarcontent = "";
    private static String s = "";

    private static ParseTree parseTree = null;

    static {
        ClassLoader classLoader = TestProcessors.class.getClassLoader();
        try {
            try (InputStream sgrammar = classLoader.getResourceAsStream("inmemantlr/Java.g4");
                 InputStream sfile = classLoader.getResourceAsStream("inmemantlr/HelloWorld.java")) {
                sgrammarcontent = FileUtils.getStringFromStream(sgrammar);
                s = FileUtils.getStringFromStream(sfile);
            }
        } catch (IOException e) {
            Assertions.assertFalse(true);
        }
        GenericParser gp = new GenericParser(sgrammarcontent);

        boolean compile;
        try {
            gp.compile();
            compile = true;
        } catch (CompilationException e) {
            compile = false;
        }

        Assertions.assertTrue(compile);
        Assertions.assertTrue(s != null && !s.isEmpty());

        DefaultTreeListener dlist = new DefaultTreeListener();

        gp.setListener(dlist);

        try {
            gp.parse(s);
        } catch (IllegalWorkflowException | ParsingException e) {
            Assertions.assertTrue(false);
        }

        parseTree = dlist.getParseTree();
    }


    @Test
    public void testXmlProcessor() {
        String xml = parseTree.toXml();
        LOGGER.debug(xml);

        DocumentBuilder newDocumentBuilder = null;
        try {
            newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            newDocumentBuilder.parse(new ByteArrayInputStream(xml
                    .getBytes()));
        } catch (SAXException e) {
            Assertions.assertFalse(true);
        } catch (IOException e) {
            Assertions.assertFalse(true);
        }

        Assertions.assertTrue(xml.length() > 1);
    }

    @Test
    public void testJsonProcessor() {
        String json = parseTree.toJson();
        LOGGER.debug(json);

        try {
            new JsonParser().parse(json);
        } catch ( JsonIOException | JsonSyntaxException e) {
            Assertions.assertFalse(true);
        }
        Assertions.assertTrue(json.length() > 1);
    }
}
