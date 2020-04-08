package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class MyConfig {
    public enum configData {
        INPUT_FILE,
        OUTPUT_FILE,
        LOG_FILE,
        ACTION
    }

    public static final String Delimeters;
    public static final String Compress;
    public static final String Decompress;

    static {
        Delimeters = "=";
        Compress = "compress";
        Decompress = "decompress";
    }

    private static HashMap<String, String> info = new HashMap<>();

    private static boolean isCorrect(HashMap<String, String> info) {
        boolean success = true;
        if (info.size() != configData.values().length) success = false;
        for (configData i : configData.values()) {
            if (!info.containsKey(i.toString())) success = false;
            if (i.toString().equals(configData.ACTION.toString())) {
                String value = info.get(i.toString());
                if (!(value.equals(Compress) || value.equals(Decompress))) success = false;
            }
        }
        return success;
    }

    public static boolean CreateMyConfig(String filename) {
        MyConfig cfg = new MyConfig();
        return cfg.ParseConfig(filename);
    }

    public boolean ParseConfig(String filename) {
        Scanner scanner = null;
        try {
            File file = new File(filename);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    line = line.replaceAll(HelpClass.Space, HelpClass.EmptyString);
                    if(!line.isEmpty()) {
                        String[] data = line.split(Delimeters);
                        if (data.length != 2)
                        {
                            if (MyLogger.createLogger(HelpClass.Error)) MyLogger.writeToLogger(MyLogger.CONFIG);
                            return false;
                        }
                        data[1] = data[1].toLowerCase();
                        info.put(data[0], data[1]);
                    }
                }
                if(!isCorrect(info))
                {
                    if (MyLogger.createLogger(HelpClass.Error)) MyLogger.writeToLogger(MyLogger.CONFIG);
                    return false;
                }
            }
            else
            {
                if (MyLogger.createLogger(HelpClass.Error)) MyLogger.writeToLogger(MyLogger.INPUT_FILE_EMPTY);
                return false;
            }
        }
        catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            if (MyLogger.createLogger(HelpClass.Error)) MyLogger.writeToLogger(MyLogger.INPUT_FILE_OPEN);
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
    public static String getAction()
    {
        return info.get(configData.ACTION.toString());
    }
    public static String getInput()
    {
        return info.get(configData.INPUT_FILE.toString());
    }
    public static String getOutput()
    {
        return info.get(configData.OUTPUT_FILE.toString());
    }
    public static String getLog()
    {
        return info.get(configData.LOG_FILE.toString());
    }
}
