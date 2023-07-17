package de.samply.logger;

import com.google.common.collect.EvictingQueue;

public class Logger {

    private EvictingQueue<String> evictingQueue;
    private org.slf4j.Logger logger;

    public Logger(EvictingQueue<String> evictingQueue, org.slf4j.Logger logger) {
        this.evictingQueue = evictingQueue;
        this.logger = logger;
    }

    public void info(String msg) {
        if (logger.isInfoEnabled()){
            evictingQueue.add(msg);
        }
        logger.info(msg);
    }

    public void error(String msg) {
        if (logger.isErrorEnabled()){
            evictingQueue.add(msg);
        }
        logger.error(msg);
    }

    public void debug(String msg) {
        if (logger.isDebugEnabled()){
            evictingQueue.add(msg);
        }
        logger.debug(msg);
    }


}
