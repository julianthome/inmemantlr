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
import org.snt.inmemantlr.utils.EscapeUtils;
import org.snt.inmemantlr.utils.FileUtils;


public class TestUtils {

    @Test
    public void testEscapeUtils() {
        String sorig = "+{}()[]&^-?*\"$<>.|#\\\"";
        String sesc = EscapeUtils.escapeSpecialCharacters(sorig);
        Assertions.assertEquals("\\+\\{\\}\\(\\)\\[\\]\\&\\^\\-\\?\\*\\\"\\$\\<\\>\\.\\|\\#\\\\\"", sesc);
        String suesc = EscapeUtils.unescapeSpecialCharacters(sesc);
        Assertions.assertEquals(sorig, suesc);

        Assertions.assertEquals("", EscapeUtils.escapeSpecialCharacters(null));
        Assertions.assertEquals("", EscapeUtils.unescapeSpecialCharacters(null));
    }

    @Test
    public void testFileUtils() {
        Assertions.assertNull(FileUtils.loadFileContent(""));
        Assertions.assertNull(FileUtils.getStringFromStream(null));
    }

}
