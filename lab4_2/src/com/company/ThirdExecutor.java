package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

//this executor has only one producer
//this executor makes all the characters uppercase

public class ThirdExecutor implements ru.spbstu.pipeline.Executor {

    private ArrayList<Consumer> m_consumers;
    private Object outdata;
    private String parameter; //просто какой-то параметр

    private Producer m_producer;
    private Producer.DataAccessor dataAccessor;
    private ArrayBlockingQueue<String> indata;

    private static final int sleeptime = 10;

    private boolean endofstreamflag;

    private static final String Space;
    private static final String EmptyString;

    private Set<String> DEFAULT_TYPES;

    private String consumer_intype; //тот тип, который принимаем

    private Logger log;
    private Status st;

    private static final String NO_CONFIG;

    static{
        NO_CONFIG = "none";
        Space = " ";
        EmptyString = "";
    }

    private boolean parseConfig(String filename)
    {
        Scanner scanner = null;
        try {
            File file = new File(filename);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                String line = scanner.nextLine();
                line = line.replaceAll(Space, EmptyString);
                parameter = line;
            }
            else
            {
                log.log("Executor config file is empty");
                return false;
            }
        }
        catch (FileNotFoundException | NullPointerException file_not_found) //file cannot be opened
        {
            log.log("Executor config is invalid");
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
    private Object doMyJob(String indata)
    {
        return indata.toUpperCase();
    }
    public ThirdExecutor(Logger logger, String config)
    {
        this.log = logger;
        this.st = Status.OK;
        endofstreamflag = false;
        consumer_intype = String.class.getCanonicalName();
        DEFAULT_TYPES = new TreeSet<>();
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        m_consumers = new ArrayList<>();
        indata = new ArrayBlockingQueue<>(1);
        if (!config.toLowerCase().equals(NO_CONFIG))
        {
            if(!parseConfig(config)) {
                st = Status.EXECUTOR_ERROR;
                log.log("Got some problems with parsing executors config");
            }
        }
    }
    public ThirdExecutor(String config, Logger logger)
    {
        this.log = logger;
        this.st = Status.OK;
        consumer_intype = String.class.getCanonicalName();
        DEFAULT_TYPES = new TreeSet<>();
        endofstreamflag = false;
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        m_consumers = new ArrayList<>();
        indata = new ArrayBlockingQueue<>(1);
        if (!config.toLowerCase().equals(NO_CONFIG))
        {
            if(!parseConfig(config)) {
                st = Status.EXECUTOR_ERROR;
                log.log("Got some problems with parsing executors config");
            }
        }
    }
    @Override
    public long loadDataFrom(@NotNull Producer prod)
    {
        String newdata = (String)dataAccessor.get();
        if (indata.isEmpty())
        {
            indata.add(newdata);
            return dataAccessor.size();
        }
        return 0;
    }
    @Override
    public void addProducers(@NotNull List<Producer> producers){
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
            m_producer = producer;
            dataAccessor = producer.getAccessor(defaulttype);
        }
    }
    @Override
    public void addConsumer(@NotNull Consumer consumer) {
        m_consumers.add(consumer);
    }
    @Override
    public void addConsumers(@NotNull List<Consumer> consumers) {
        for (Consumer i : consumers) {
            if (i != null) m_consumers.add(i);
            else log.log("Tried to add null consumer in executor");
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
            if (indata.isEmpty()) {
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    log.log("Unable put to sleep thread in executor");
                    return;
                }
            } else {
                while (!indata.isEmpty())
                {
                    String data = indata.poll();
                    if (Arrays.equals(data.getBytes(), MyReader.endoffile))
                    {
                        endofstreamflag = true;
                        outdata = data;
                    }
                    else outdata = doMyJob(data);
                        for (Consumer i : m_consumers) {
                            if (this.status() == Status.OK) {
                                if(i.status() == Status.OK) {
                                    while (i.loadDataFrom(this) == 0) //загружаем данные в консумеры из ридера
                                    {
                                        try {
                                            Thread.sleep(sleeptime);
                                        } catch (InterruptedException e) {
                                            log.log("Unable put to sleep thread in executor");
                                            return;
                                        }
                                    }
                                    this.st = i.status();
                                }
                            }
                        }
                }
            }
        }
    }
    public final class CharArrayAccessor implements Producer.DataAccessor
    {
        @NotNull
        @Override
        public char[] get() {
            Objects.requireNonNull(outdata);
            return ((String)outdata).toCharArray();
        }
        @Override
        public long size() {
            Objects.requireNonNull(outdata);
            char[] help = ((String)outdata).toCharArray();
            return help.length;
        }
    }
    public final class ByteArrayAccessor implements Producer.DataAccessor
    {
        @NotNull
        @Override
        public byte[] get() {
            Objects.requireNonNull(outdata);
            return ((String)outdata).getBytes();
        }
        @Override
        public long size() {
            Objects.requireNonNull(outdata);
            byte[] help = ((String)outdata).getBytes();
            return help.length;
        }
    }
    public final class StringAccessor implements Producer.DataAccessor
    {
        @NotNull
        @Override
        public String get() {
            Objects.requireNonNull(outdata);
            return (String) outdata;
        }
        @Override
        public long size() {
            Objects.requireNonNull(outdata);
            return ((String) outdata).length();
        }
    }
    @Override
    @NotNull
    public Producer.@NotNull DataAccessor getAccessor(@NotNull String typename){
        if (typename.equals(String.class.getCanonicalName())) return new StringAccessor();
        if (typename.equals(byte[].class.getCanonicalName())) return new ByteArrayAccessor();
        if (typename.equals(char[].class.getCanonicalName())) return new CharArrayAccessor();
        return new Producer.DataAccessor() {
            @NotNull
            @Override
            public Object get() {
                return outdata;
            }
            @Override
            public long size() {
                return 0;
            }
        };
    }
    @Override
    @NotNull
    public Set<String> outputDataTypes()
    {
        if(DEFAULT_TYPES == null) {
            log.log("No output types found");
            st = Status.EXECUTOR_ERROR;
        }
        return DEFAULT_TYPES;
    }
}
