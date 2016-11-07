package org.snt.inmemantlr.utils;

import org.slf4j.Logger;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

/**
 * Created by julian on 07/11/2016.
 */
public class SimpleErrorListener implements STErrorListener {

    private Logger logger = null;
    public SimpleErrorListener (Logger logger){
        this.logger = logger;
    }

    @Override
    public void compileTimeError(STMessage stMessage) {
        logger.error(stMessage.toString());
    }

    @Override
    public void runTimeError(STMessage stMessage) {
        logger.error(stMessage.toString());
    }

    @Override
    public void IOError(STMessage stMessage) {
        logger.error(stMessage.toString());
    }

    @Override
    public void internalError(STMessage stMessage) {
        logger.error(stMessage.toString());
    }
}
