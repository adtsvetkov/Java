package com.company;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
    private static Logger log;

    private static final String message;

    public static final String WRONG_ARGUMENTS;
    public static final String INPUT_FILE_OPEN;
    public static final String LINE_EMPTY;
    public static final String INPUT_FILE_EMPTY;
    public static final String BUFFERED_READER;
    public static final String FILE_WRITER;
    public static final String SORTING_FUNCTION;
    public static final String LINE_SYMBOLS;
    public static final String INPUT_FILE_INVALID;
    public static final String ARITHMETICAL_ERROR;
    public static final String CONFIG;

    public static final String SUCCESS;

    private static final String LOGERR_CREATE;

    static {
        message = MyLogger.class.getName();

        WRONG_ARGUMENTS = "#0 Wrong arguments. No config file found";
        INPUT_FILE_OPEN = "#1 Unable to open input file";
        LINE_EMPTY = "#2 Line is empty by reading input file";
        INPUT_FILE_EMPTY = "#3 Input file is empty";
        BUFFERED_READER = "#4 Unable to create BufferedReader by reading input file";
        FILE_WRITER = "#5 Unable to create FileWriter by writing output data";
        SORTING_FUNCTION = "#6 Unable to compress: sorting function failed";
        LINE_SYMBOLS = "#7 Unable to compress: some line symbols not found in data";
        INPUT_FILE_INVALID = "#8 Unable to decompress: invalid input file was given";
        ARITHMETICAL_ERROR = "#9 Unable to decompress: arithmetical error";
        CONFIG = "#10 Config file is invalid";

        SUCCESS = "#11 Process finished";

        LOGERR_CREATE = "Unable to create logger";
    }


    public static boolean createLogger(String loggerinfo) {
        log = Logger.getLogger(message); //initialisation with name
        try {
            FileHandler fhandler = new FileHandler(loggerinfo);  //writing stream
            SimpleFormatter sformatter = new SimpleFormatter(); //writing format
            fhandler.setFormatter(sformatter);
            log.addHandler(fhandler);
        } catch (IOException e) {
            System.out.println(LOGERR_CREATE);
        }
        return true;
    }

    public static void writeToLogger(String Errmsg) {
        log.severe(Errmsg);
    }
}
