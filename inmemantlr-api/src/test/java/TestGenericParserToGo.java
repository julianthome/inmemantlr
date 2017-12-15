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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.GenericParserToGo;
import org.snt.inmemantlr.tree.ParseTree;

import java.io.File;

;


public class TestGenericParserToGo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestGenericParserToGo.class);

    static File grammar = null;
    static File fgrammar = null;
    static File sfile = null;

    static {
        ClassLoader classLoader = TestGenericParserToGo.class.getClassLoader();
        grammar = new File(classLoader.getResource("inmemantlr/Java.g4").getFile());
        fgrammar = new File(classLoader.getResource("inmemantlr/Fail.g4")
                .getFile());
        sfile = new File(classLoader.getResource("inmemantlr/HelloWorld.java").getFile());
    }

    @Test
    public void testParser() {
        GenericParserToGo g1 = new GenericParserToGo(grammar);
        ParseTree pt = g1.parse(sfile);
        LOGGER.info(pt.toDot());
        Assertions.assertEquals(pt.getNodes().size(), 60);
    }


}
