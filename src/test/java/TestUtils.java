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
import org.snt.inmemantlr.utils.EscapeUtils;
import org.snt.inmemantlr.utils.FileUtils;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    @Test
    public void testEscapeUtils() {
        String sorig = "+{}()[]&^-?*\"$<>.|#\\\"";
        String sesc = EscapeUtils.escapeSpecialCharacters(sorig);
        assertEquals(sesc, "\\+\\{\\}\\(\\)\\[\\]\\&\\^\\-\\?\\*\\\"\\$\\<\\>\\.\\|\\#\\\\\"");
        String suesc = EscapeUtils.unescapeSpecialCharacters(sesc);
        assertEquals(suesc, sorig);

        assertEquals(EscapeUtils.escapeSpecialCharacters(null), "");
        assertEquals(EscapeUtils.unescapeSpecialCharacters(null), "");
    }

    @Test
    public void testFileUtils() {
        assertEquals(FileUtils.loadFileContent(""), null);
        assertEquals(FileUtils.getStringFromStream(null), null);
    }
}
