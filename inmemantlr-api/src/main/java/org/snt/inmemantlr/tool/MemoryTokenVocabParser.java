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

package org.snt.inmemantlr.tool;

import org.antlr.runtime.Token;
import org.antlr.v4.codegen.CodeGenerator;
import org.antlr.v4.parse.TokenVocabParser;
import org.antlr.v4.tool.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * special memory token vocab parser that operates in-memory
 */
public class MemoryTokenVocabParser extends TokenVocabParser {
    private Scanner tv;

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTokenVocabParser.class);

    public MemoryTokenVocabParser(Grammar g, String tokenVocab) {
        super(g);
        tv = new Scanner(tokenVocab);
    }

    @Override
    public Map<String, Integer> load() {
        Map<String, Integer> tokens = new LinkedHashMap<>();
        int maxTokenType = -1;
        String vocabName = g.getOptionString("tokenVocab");

        if (!tv.hasNextLine())
            return tokens;

        Pattern tokenDefPattern = Pattern.compile("([^\n]+?)[ \\t]*?=[ \\t]*?([0-9]+)");

        int lineNum = 1;
        while (tv.hasNextLine()) {
            String tokenDef = tv.nextLine();
            Matcher matcher = tokenDefPattern.matcher(tokenDef);
            if (matcher.find()) {
                String tokenID = matcher.group(1);
                String tokenTypeS = matcher.group(2);
                int tokenType;
                try {
                    tokenType = Integer.valueOf(tokenTypeS);
                } catch (NumberFormatException nfe) {
                    LOGGER.error(vocabName + CodeGenerator.VOCAB_FILE_EXTENSION,
                            " bad token type: " + tokenTypeS,
                            lineNum);
                    tokenType = Token.INVALID_TOKEN_TYPE;
                    assert false;
                    System.exit(-1);
                }
                tokens.put(tokenID, tokenType);
                maxTokenType = Math.max(maxTokenType, tokenType);
                lineNum++;
            } else {
                if (tokenDef.length() > 0) { // ignore blank lines
                    LOGGER.error(vocabName + CodeGenerator.VOCAB_FILE_EXTENSION,
                            " bad token def: " + tokenDef,
                            lineNum);
                    assert false;
                    System.exit(-1);
                }
            }
        }

        return tokens;
    }
}
