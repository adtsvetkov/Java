package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyManagerConfig {
    private static MyLogger logger;
    private static List<Pairs> executorsdata;
    public enum ConfigData
    {
        WRITER,
        READER,
        EXECUTOR,
        EXECUTORSCONFIG,
        EXECUTORSNUMBER
    }
    public static final String Delimeters;
    public static final String Space;
    public static final String EmptyString;

    static
    {
        Delimeters = "=";
        Space = " ";
        EmptyString = "";
    }
    //один райтер, один ридер, любое количество эксекьюторов
    private static boolean isCorrect() {
        int writerflag = 0;
        int readerflag = 0;
        for (int i=0; i<executorsdata.size(); i++) {
            boolean flag = false;
            String value = executorsdata.get(i).getKey();
            for (ConfigData c : ConfigData.values())
            {
                if(c.toString().equals(value)) flag = true;
            }
            if (!flag) return false;
            if (ConfigData.EXECUTOR.toString().equals(value))
            {
                if (!ConfigData.EXECUTORSCONFIG.toString().equals(executorsdata.get(i+1).getKey())) return false;
            }
            if (ConfigData.READER.toString().equals(value)) readerflag++;
            if (ConfigData.WRITER.toString().equals(value)) writerflag++;
            if (ConfigData.EXECUTORSNUMBER.toString().equals(value))
            {
                if (!ConfigData.EXECUTOR.toString().equals(executorsdata.get(i+1).getKey())) return false;
            }
        }
        if (!((readerflag==1) && (writerflag==1))) return false;
        return true;
    }

    private static boolean ParseConfig(String configname)
    {
        Scanner scanner = null;
        try {
            File file = new File(configname);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                executorsdata = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    line = line.replaceAll(Space, EmptyString);
                    if(!line.isEmpty()) {
                        String[] data = line.split(Delimeters);
                        if (data.length != 2)
                        {
                            logger.writeToLogger(MyLogger.CONFIG);
                            return false;
                        }
                        data[0] = data[0].toUpperCase();
                        Pairs datapair = new Pairs();
                        datapair.setValues(data[0], data[1]);
                        executorsdata.add(datapair);
                    }
                }
                if(!isCorrect())
                {
                    logger.writeToLogger(MyLogger.CONFIG);
                    return false;
                }
            }
            else
            {
                logger.writeToLogger(MyLogger.INPUT_FILE_EMPTY);
                return false;
            }
        }
        catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            logger.writeToLogger(MyLogger.INPUT_FILE_OPEN);
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
    public static List<Pairs> getData(String configname, MyLogger log)
    {
        logger = log;
        if (ParseConfig(configname)) return executorsdata;
        return null;
    }
}
