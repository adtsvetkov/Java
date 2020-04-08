package com.company;

import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.UtilLogger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
    public Status status;

    private static Logger log;
    private static UtilLogger logger;

    private static final String message;
    private static final String loggerinfo;

    public static final String LOGERR_CREATE;
    public static final String WRONG_ARGUMENTS;
    public static final String CONFIG;
    public static final String INPUT_FILE_EMPTY;
    public static final String INPUT_FILE_OPEN;
    public static final String EXECUTOR_CONFIG;

    static {
        message = MyLogger.class.getName();

        LOGERR_CREATE = "Unable to create logger";
        WRONG_ARGUMENTS = "Wrong arguments. No config file found";
        CONFIG = "Config file is invalid";
        INPUT_FILE_EMPTY = "Input file is empty";
        INPUT_FILE_OPEN = "Unable to open input file";
        EXECUTOR_CONFIG = "Config error: executors data is invalid";

        loggerinfo = "log.log";
    }
    MyLogger()
    {
        if(createLogger()) status = Status.OK;
        else status = Status.ERROR;
    }
    private static boolean createLogger() {
        log = Logger.getLogger(message); //initialisation with name
        try {
            FileHandler fhandler = new FileHandler(loggerinfo);  //writing stream
            SimpleFormatter sformatter = new SimpleFormatter(); //writing format
            fhandler.setFormatter(sformatter);
            log.addHandler(fhandler);
            logger = UtilLogger.of(log);
        } catch (IOException e) {
            System.out.println(LOGERR_CREATE);
        }
        return true;
    }
    public Logger getLogger()
    {
        return log;
    }
    public UtilLogger getUtilLogger()
    {
        return logger;
    }
    public void writeToLogger(String Errmsg) {
        logger.log(Errmsg);
    }
}
