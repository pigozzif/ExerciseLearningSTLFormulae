package com.eggloop.flow.learning;

import java.util.logging.*;

 class LoggerFactory {
    static Logger get(){
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return super.formatMessage(record) + "\n";
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        handler.setLevel(Level.INFO);
        return logger;
    }
}
