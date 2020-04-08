package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.util.*;

public class MyReader implements MyReaderInterface {
    private Logger log;
    private ArrayList<Consumer> consumers;
    private String filename;
    private int readsize;
    private Status st;
    private Object buffer;

    private static final String Space;
    private static final String EmptyString;

    private Set<String> DEFAULT_TYPES;

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
        DEFAULT_TYPES = new TreeSet<>();
        DEFAULT_TYPES.add(byte.class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        if(!readConfig(configname))
        {
            this.st = Status.READER_ERROR;
            log.log("Got some problems with parsing reader config");
        }
    }

    public MyReader (String configname, Logger log)
    {
        consumers = new ArrayList<>();
        filename = null;
        readsize = invalidbuffersize;
        this.log = log;
        this.st = Status.OK;
        DEFAULT_TYPES = new TreeSet<>();
        DEFAULT_TYPES.add(byte.class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
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
    @NotNull
    @Override
    public Status status(){
        return this.st;
    }
    @Override
    public void run() {
        buffer = new byte[readsize];
        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(filename);
            while (fileInputStream.read((byte[])buffer)>0) {
                for (Consumer i : consumers)
                {
                    if(this.status() == Status.OK) {
                        if (i.loadDataFrom(this) != 0) //загружаем данные в консумеры из ридера
                        {
                            i.run();
                            this.st = i.status();
                        }
                        else
                        {
                            st = Status.EXECUTOR_ERROR;
                            log.log("Data size in producer is null");
                            return;
                        }
                    }
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
    public void addConsumer(@NotNull Consumer consumer) {
        consumers.add(consumer);
    }
    @Override
    public void addConsumers(@NotNull List<Consumer> consumers){
        for (Consumer c: consumers)
        {
            if (c!=null) this.consumers.add(c);
            else log.log("Tried to add null consumer in reader");
        }
    }
    public final class CharArrayAccessor implements Producer.DataAccessor
    {
        @NotNull
        @Override
        public char[] get() {
            Objects.requireNonNull(buffer);
            String buf = new String((byte[])buffer);
            return buf.toCharArray();
        }
        @Override
        public long size() {
            Objects.requireNonNull(buffer);
            String buf = new String((byte[])buffer);
            char[] buff = buf.toCharArray();
            return buff.length;
        }
    }
    public final class ByteArrayAccessor implements Producer.DataAccessor
    {
        @NotNull
        @Override
        public byte[] get() {
            Objects.requireNonNull(buffer);
            return (byte[])buffer;
        }
        @Override
        public long size() {
            Objects.requireNonNull(buffer);
            return ((byte[]) buffer).length;
        }
    }
    public final class StringAccessor implements Producer.DataAccessor
    {
        private String str;
        @NotNull
        @Override
        public String get() {
            Objects.requireNonNull(buffer);
            str = new String((byte[])buffer);
            return str;
        }
        @Override
        public long size() {
            Objects.requireNonNull(buffer);
            return str.length();
        }
    }
    @Override
    @NotNull
    public DataAccessor getAccessor(@NotNull String typename){
        if (typename.equals(String.class.getCanonicalName())) return new StringAccessor();
        if (typename.equals(byte[].class.getCanonicalName())) return new ByteArrayAccessor();
        if (typename.equals(char[].class.getCanonicalName())) return new CharArrayAccessor();
        return new DataAccessor() {
            @NotNull
            @Override
            public Object get() {
                return buffer;
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
        }
        return DEFAULT_TYPES;
    }
}