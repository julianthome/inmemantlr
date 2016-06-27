package org.snt.inmemantlr.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EscapeUtils {

    private static Character[] sarray = new Character[]{'+', '{', '}', '(', ')', '[', ']', '&', '^', '-', '?', '*', '\"', '$', '<', '>', '.', '|', '#'};
    private static Set<Character> special = new HashSet<Character>(Arrays.asList(sarray));


    public static String escapeSpecialCharacters(String s) {
        StringBuilder out = new StringBuilder();
        char pred = ' ';
        for (char c : s.toCharArray()) {
            if (pred != '\\' && special.contains(c)) {
                out.append("\\" + c);
            } else if (pred == '\\' && special.contains(c)) {
                out.deleteCharAt(out.length() - 1); // delete NULL
                out.append(c);
            } else {
                out.append(c);
            }
            pred = c;
        }
        return out.toString();
    }
}