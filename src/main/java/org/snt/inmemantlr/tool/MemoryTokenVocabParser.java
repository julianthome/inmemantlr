package org.snt.inmemantlr.tool;

import org.antlr.runtime.Token;
import org.antlr.v4.Tool;
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


public class MemoryTokenVocabParser extends TokenVocabParser {
    private Scanner tv;

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTokenVocabParser.class);

    public MemoryTokenVocabParser(Grammar g, String tokenVocab) {
        super(g);
        tv = new Scanner(tokenVocab);
    }

    @Override
    public Map<String, Integer> load() {
        Map<String, Integer> tokens = new LinkedHashMap<String, Integer>();
        int maxTokenType = -1;
        Tool tool = g.tool;
        String vocabName = g.getOptionString("tokenVocab");

        if(!tv.hasNextLine())
            return tokens;

        Pattern tokenDefPattern = Pattern.compile("([^\n]+?)[ \\t]*?=[ \\t]*?([0-9]+)");

        String tokenDef = tv.nextLine();
        int lineNum = 1;
        while (tv.hasNextLine()) {
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
                }
                tokens.put(tokenID, tokenType);
                maxTokenType = Math.max(maxTokenType, tokenType);
                lineNum++;
            } else {
                if (tokenDef.length() > 0) { // ignore blank lines
                    LOGGER.error(vocabName + CodeGenerator.VOCAB_FILE_EXTENSION,
                            " bad token def: " + tokenDef,
                            lineNum);
                }
            }
            tokenDef = tv.nextLine();
        }

        return tokens;
    }
}
