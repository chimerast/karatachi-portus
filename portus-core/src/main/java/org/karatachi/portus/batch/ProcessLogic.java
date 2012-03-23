package org.karatachi.portus.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessLogic implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String[] args = Bootstrap.args;
}
