package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.*;
import ru.spbstu.pipeline.Writer;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.util.*;

public class MyWriter implements Writer {
    private Status st;
    private List<Triplet> producers;
    private Logger log;
    private String outputfile;
    private int bytedatasize;
    private FileOutputStream foutput;

    private static final int sleeptime = 10;

    private boolean endofstreamflag;

    private String consumer_intype;

    private static final String Space;
    private static final String EmptyString;

    private boolean checknulls(List<Triplet> list)
    {
        for (int i=0; i<list.size(); i++)
        {
           if(list.get(i).getData().isEmpty()) return false;
        }
        return true;
    }
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
        endofstreamflag = false;
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
        producers = new ArrayList<>();
        endofstreamflag = false;
        foutput = null;
        consumer_intype = byte[].class.getCanonicalName();
        if(!readConfig(configname))
        {
            this.st = Status.WRITER_ERROR;
            log.log("Got some problems with parsing writer config");
        }
    }

    private int findinTriplet(List<Triplet> list, Producer prod)
    {
        for (int i=0; i<list.size(); i++)
        {
            if (list.get(i).getProducer() == prod) return i;
        }
        return 0;
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
                    try {
                        bytedatasize = Integer.parseInt(line);
                    }catch (NullPointerException e) {
                        log.log("Writer config is invalid");
                        return false;
                    }
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

    @Override
    public long loadDataFrom(@NotNull Producer prod) {
        int index = findinTriplet(producers, prod);
        Producer.DataAccessor dataAccessor = producers.get(index).getDataAccessor();
        byte [] helparray = (byte[])dataAccessor.get();
        byte [] newdata = Arrays.copyOf(helparray, helparray.length);
        if (producers.get(index).getData().isEmpty())
        {
            if (Arrays.equals(newdata, MyReader.endoffile)) endofstreamflag = true;
            else producers.get(index).setData(newdata);
            return dataAccessor.size();
        }
        return 0;

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
                    Triplet tr = new Triplet();
                    tr.setElems(i, i.getAccessor(defaulttype));
                    this.producers.add(tr);
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
            Triplet tr = new Triplet();
            tr.setElems(producer, producer.getAccessor(defaulttype));
            producers.add(tr);
        }
    }
    @Override
    @NotNull
    public Status status() {
        return this.st;
    }
    @Override
    public void run() {
            while (!endofstreamflag) {
            if (!checknulls(producers))
            {
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    log.log("Unable put to sleep thread in writer");
                    return;
                }
            }
            else {
                    try {
                        if (foutput == null) foutput = new FileOutputStream(outputfile);
                        for (int i = 0; i < producers.size(); i++) {
                            byte[] data = (byte[])producers.get(i).getData().poll();
                            foutput.write(data);
                        }
                    } catch (IOException e) {
                        log.log("Writer output file cannot be opened");
                    }
                }
            }
           closeStream();
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
