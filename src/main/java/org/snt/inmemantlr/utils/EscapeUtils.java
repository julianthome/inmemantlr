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

package org.snt.inmemantlr.utils;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * escaping helper class
 */
public final class EscapeUtils {

    private static final Set<Character> SPECIAL = Stream.of('+', '{', '}', '(', ')', '[', ']', '&', '^',
            '-', '?', '*', '\"', '$', '<', '>', '.', '|', '#').collect(toSet());

    private EscapeUtils() {
    }

    /**
     * escape special character in a string with a backslash
     *
     * @param s string to be escaped
     * @return escaped string
     */
    public static String escapeSpecialCharacters(String s) {
        if (s == null)
            return "";

        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (SPECIAL.contains(c)) {
                out.append("\\").append(c);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * unescape special character in a string
     *
     * @param s string to be unescaped
     * @return unescaped string
     */
    public static String unescapeSpecialCharacters(String s) {
        if (s == null)
            return "";

        StringBuilder out = new StringBuilder();
        char pred = ' ';
        for (char c : s.toCharArray()) {
            if (pred == '\\' && SPECIAL.contains(c)) {
                out.deleteCharAt(out.length() - 1);
                out.append(c);
            } else {
                out.append(c);
            }
            pred = c;
        }
        return out.toString();
    }
}