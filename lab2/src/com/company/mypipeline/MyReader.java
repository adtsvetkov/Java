package com.company.mypipeline;

import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Reader;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyReader implements Reader {
    private Logger log;
    private ArrayList<Consumer> consumers;
    private String filename;
    private int readsize;
    private Status st;
    private byte[] buffer;

    private static final String Space;
    private static final String EmptyString;

    private static final int filesize = 2;
    private static final int invalidbuffersize = -1; //invalid buffer size
    private static final int maxbuffersize = 255; //default the biggest size

    static{
        Space = " ";
        EmptyString = "";
    }

    public MyReader(Logger log, String configname) {
        consumers = new ArrayList<>();
        filename = null;
        readsize = invalidbuffersize;
        this.log = log;
        this.st = Status.OK;
        if(!readConfig(configname))
        {
            this.st = Status.READER_ERROR;
            log.log("Got some problems with parsing reader config");
        }
    }

    public MyReader (String configname, Logger log)
    {
        this.consumers = new ArrayList<>();
        this.filename = null;
        this.readsize = invalidbuffersize;
        this.log = log;
        if(!readConfig(configname))
        {
            this.st = Status.READER_ERROR;
            log.log("Got some problems with parsing reader config");
        }
    }

    private boolean readConfig(String configname)
    {
        Scanner scanner = null;
        try {
            File file = new File(configname);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                int i = 0;
                while (scanner.hasNextLine() && i<filesize) {
                    String line = scanner.nextLine();
                    line = line.replaceAll(Space, EmptyString);
                    switch (i){
                        case 0: this.filename = line;
                            break;
                        case 1: this.readsize = Integer.parseInt(line);
                            if (readsize<=0) readsize = 1;
                            if (readsize>maxbuffersize) readsize = maxbuffersize;
                            break;
                    }
                    i++;
                }
            }
            else
            {
                log.log("Reader config file is empty");
                return false;
            }
        }
        catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            log.log("Reader config file cannot be opened");
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        if (readsize == invalidbuffersize && filename == null) return false;
        else return true;
    }
    @Override
    public Status status() {
        return this.st;
    }
    @Override
    public void run() {
        buffer = new byte[readsize];
        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(filename);
            while (fileInputStream.read(buffer)>0) {
                for (Consumer i : consumers)
                {
                    if(this.status() == Status.OK) i.loadDataFrom(this);
                    i.run();
                }
                buffer = new byte[readsize]; //очищаем буфер на случай, если размер последнего блока считывания меньше того, которым читаем; в случае идеального мира просто закомментировать эту строку
            }
        }
        catch (IOException e)
        {
            this.st = Status.READER_ERROR;
        }
        finally
        {
            try {
               if(fileInputStream!=null) fileInputStream.close();
            }
            catch (IOException e)
            {
                log.log("Something went wrong with fileinputstream in MyReader. Cannot be closed");
                this.st = Status.READER_ERROR;
            }
        }
    }
    @Override
    public void addConsumer(Consumer consumer) {
        if (consumer != null) consumers.add(consumer);
        else log.log("Tried to add null consumer in reader");
    }
    @Override
    public void addConsumers(List<Consumer> consumers){
        for (Consumer c: consumers)
        {
            if (c!=null) this.consumers.add(c);
            else log.log("Tried to add null consumer in reader");
        }
    }
    @Override
    public Object get() {
        return buffer;
    }
}