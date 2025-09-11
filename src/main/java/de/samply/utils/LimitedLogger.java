package de.samply.utils;

import de.samply.logger.Logger;

public class LimitedLogger {
    private Logger logger;
    private int counter = 0;
    private int maxErrors;

    public LimitedLogger(Logger logger, int maxErrors) {
        this.logger = logger;
    }

    public void error(String message) {
        if (counter >= maxErrors) {
            logger.debug(message);
        } else {
            logger.error(message);
        }
        counter++;
        if (counter == maxErrors) {
            logger.error("Maximum number of errors to be logged reached. Please set log level to debug to see all the logs");
        }
    }

}
