package org.snt.inmemantlr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.exceptions.CompilationException;
import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
import org.snt.inmemantlr.exceptions.ParsingException;
import org.snt.inmemantlr.listener.DefaultTreeListener;
import org.snt.inmemantlr.tree.ParseTree;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * a simpler generic parser representation to get going quicly
 */
public class GenericParserToGo {

    private GenericParser gp;
    private DefaultTreeListener dl;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParserToGo.class);

    /**
     * constructor
     * @param grammar file path to antlr grammar
     */
    public GenericParserToGo(String grammar) {
        this(new File(grammar));
    }

    /**
     * constructor
     * @param grammar file path to antlr grammar
     */
    public GenericParserToGo(File grammar) {
        try {
            gp = new GenericParser(grammar);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        try {
            gp.compile();
        } catch (CompilationException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        dl = new DefaultTreeListener(true);
        gp.setListener(dl);
    }


    /**
     * parse a string
     * @param s a string to parse
     * @return parse tree of s
     */
    public ParseTree parse(String s, String rule){
        try {
            gp.parse(s, rule, GenericParser.CaseSensitiveType.NONE);
            return dl.getParseTree();
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        } catch (ParsingException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * parse a string
     * @param f a file to parse
     * @return parse tree of f'c content
     */
    public ParseTree parse(File f, String rule){
        try {
            gp.parse(f, rule, GenericParser.CaseSensitiveType.NONE);
            return dl.getParseTree();
        } catch (IllegalWorkflowException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        } catch (ParsingException e) {
            LOGGER.error(e.getMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
        return null;
    }

}
