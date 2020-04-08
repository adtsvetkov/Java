package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.*;
import ru.spbstu.pipeline.Writer;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.util.*;

public class MyWriter implements Writer {
    private Status st;
    private HashMap<Producer,Producer.DataAccessor> producers;
    private Logger log;
    private String outputfile;
    private Object data;
    private int bytedatasize;
    private FileOutputStream foutput;

    private String consumer_intype;

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
        producers = new HashMap<>();
        foutput = null;
        consumer_intype = byte[].class.getCanonicalName();
        if(!readConfig(configname))
        {
            this.st = Status.WRITER_ERROR;
            log.log("Got some problems with parsing writer config");
        }
    }
    public MyWriter(String configname, Logger log) {
        this.st = Status.OK;
        this.log = log;
        producers = new HashMap<>();
        foutput = null;
        consumer_intype = byte[].class.getCanonicalName();
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
    public long loadDataFrom(@NotNull Producer prod) {
        Producer.DataAccessor dataAccessor = producers.get(prod);
        data = dataAccessor.get();
        long kek = dataAccessor.size();
        if (kek != bytedatasize) return 0;
        else return dataAccessor.size();
    }
    @Override
    public void addProducers(@NotNull List<Producer> producers){
        for (Producer i: producers) {
            if (i == null) {
                st = Status.EXECUTOR_ERROR;
                log.log("Tried to add null producer");
            } else {
                Set<String> producerstypes = i.outputDataTypes();
                String defaulttype = null;
                for (String p : producerstypes) {
                    if (p.equals(consumer_intype))
                    {
                        defaulttype = p;
                        break;
                    }
                }
                if (defaulttype == null) {
                    st = Status.EXECUTOR_ERROR;
                    log.log("Acquaintance failed");
                } else {
                    this.producers.put(i, i.getAccessor(defaulttype));
                }
            }
        }
    }
    @Override
    public void addProducer(@NotNull Producer producer) {
        Set<String> producerstypes = producer.outputDataTypes();
        String defaulttype = null;
        for (String p: producerstypes)
        {
            if (p.equals(consumer_intype))
            {
                defaulttype = p;
                break;
            }
        }
        if (defaulttype==null)
        {
            st = Status.EXECUTOR_ERROR;
            log.log("Acquaintance failed");
        }
        else {
            producers.put(producer, producer.getAccessor(defaulttype));
        }
    }
    @Override
    @NotNull
    public Status status() {
        return this.st;
    }
    @Override
    public void run() {
            try {
                if (foutput == null) foutput = new FileOutputStream(outputfile);
                foutput.write((byte[])data);
            } catch (IOException e) {
                log.log("Writer output file cannot be opened");
            }
    }
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
