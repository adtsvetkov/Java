package com.company.mypipeline;

import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.Writer;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyWriter implements Writer {
    private Status st;
    private ArrayList<Producer> producers;
    private Logger log;
    private String outputfile;
    private byte[] data;
    private int bytedatasize;
    private FileOutputStream foutput;

    private static final String Space;
    private static final String EmptyString;

    static
    {
        Space = " ";
        EmptyString = "";
    }

    public MyWriter(Logger log, String configname) {
        this.st = Status.OK;
        this.log = log;
        producers = new ArrayList<>();
        foutput = null;
        if(!readConfig(configname))
        {
            this.st = Status.WRITER_ERROR;
            log.log("Got some problems with parsing writer config");
        }
    }
    public MyWriter(String configname, Logger log) {
        this.st = Status.OK;
        this.log = log;
        producers = new ArrayList<>();
        foutput = null;
        if(!readConfig(configname))
        {
            this.st = Status.WRITER_ERROR;
            log.log("Got some problems with parsing writer config");
        }
    }

    private boolean readConfig(String configname) {
        Scanner scanner = null;
        try {
            File file = new File(configname);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                String line = scanner.nextLine();
                outputfile = line.replaceAll(Space, EmptyString);
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    bytedatasize = Integer.parseInt(line);
                }
            }
            else
            {
                log.log("Writer config file is empty");
                return false;
            }
        }
        catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            log.log("Writer config file cannot be opened");
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
    //у нас идеальный мир, поэтому data.length = bufferdatasize
    @Override
    public void loadDataFrom(Producer producer) {
       if (producer.status() == Status.OK) {
           data = (byte[]) producer.get();
           try {
               if (foutput == null) foutput = new FileOutputStream(outputfile);
               foutput.write(data);
           } catch (IOException e) {
               log.log("Writer output file cannot be opened");
           }
       }
    }
    @Override
    public void addProducers(List<Producer> producers){
        for (Producer c: producers)
        {
            if (c!=null) this.producers.add(c);
            else log.log("Tried to add null consumer in writer");
        }
    }
    @Override
    public void addProducer(Producer producer) {
        if (producer != null) producers.add(producer);
        else log.log("Tried to add null producer in writer");
    }
    @Override
    public Status status() {
        return this.st;
    }
    @Override
    public void run() {}
    public void closeStream()
    {
        try{
            foutput.close();
        }catch (IOException e)
        {
            log.log("Writer output cannot be closed");
        }
    }
}
