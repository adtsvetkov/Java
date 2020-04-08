package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FirstExecutor implements ru.spbstu.pipeline.Executor{
    private long waitingsize;
    private static final int filesize = 2;
    private static final int invalidbuffersize = -1; //invalid buffer size

    private ArrayList<Consumer> m_consumers;
    private HashMap<Producer,Producer.DataAccessor> m_producers;
    private String indata;
    private Object outdata;
    private String parameter; //просто какой-то параметр

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
                int i = 0;
                while (scanner.hasNextLine() && i<filesize) {
                    String line = scanner.nextLine();
                    line = line.replaceAll(Space, EmptyString);
                    switch (i){
                        case 0: parameter = line;
                            break;
                        case 1: waitingsize = Integer.parseInt(line);
                            if (waitingsize<=0) waitingsize = invalidbuffersize;
                            break;
                    }
                    i++;
                }
            }
            else
            {
                log.log("Executor config file is empty");
                return false;
            }
        }
        catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            log.log("Executor config file cannot be opened");
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
    private Object doMyJob()
    {
        String b = "";
        for (int i=0; i<indata.length(); i++)
        {
            if (Character.isLetter(indata.charAt(i)))
            {
                if (Character.isLowerCase(indata.charAt(i)))
                    b+=Character.toUpperCase(indata.charAt(i));
                else b+=Character.toLowerCase(indata.charAt(i));
            }
            else b+=indata.charAt(i);
        }
        return b;
    }
    public FirstExecutor(Logger logger, String config)
    {
        this.log = logger;
        this.st = Status.OK;
        consumer_intype = String.class.getCanonicalName();
        DEFAULT_TYPES = new TreeSet<>();
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        waitingsize = invalidbuffersize;
        m_consumers = new ArrayList<>();
        m_producers = new HashMap<>();
        if (!config.toLowerCase().equals(NO_CONFIG))
        {
            if(!parseConfig(config)) {
                st = Status.EXECUTOR_ERROR;
                log.log("Got some problems with parsing executors config");
            }
        }
    }
    public FirstExecutor(String config, Logger logger)
    {
        this.log = logger;
        this.st = Status.OK;
        consumer_intype = String.class.getCanonicalName();
        DEFAULT_TYPES = new TreeSet<>();
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        waitingsize = invalidbuffersize;
        m_consumers = new ArrayList<>();
        m_producers = new HashMap<>();
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
        Producer.DataAccessor dataAccessor = m_producers.get(prod);
        indata = (String) dataAccessor.get();
        if (waitingsize == invalidbuffersize) waitingsize = dataAccessor.size();
        if (dataAccessor.size() != waitingsize) return 0;
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
                    m_producers.put(i, i.getAccessor(defaulttype));
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
            m_producers.put(producer, producer.getAccessor(defaulttype));
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
        outdata = doMyJob();
        if(outdata == null) this.st = Status.EXECUTOR_ERROR;
        else {
            for (Consumer i : m_consumers) {
                if (this.status() == Status.OK) {
                    if (i.loadDataFrom(this) != 0) //загружаем данные в консумеры из ридера
                    {
                        i.run();
                        this.st = i.status();
                    } else {
                        st = Status.EXECUTOR_ERROR;
                        log.log("Data size in producer is null");
                        return;
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
