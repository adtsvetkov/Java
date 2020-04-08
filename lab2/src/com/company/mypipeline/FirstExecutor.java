package com.company.mypipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

//меняет большие буквы на маленькие, а маленькие на большие

public class FirstExecutor implements Executor {
    private ArrayList<Consumer> m_consumers;
    private ArrayList<Producer> m_producers;
    private Object indata;
    private Object outdata;
    private ArrayList<String> parameters; //просто какие-то параметры

    private Logger log;
    private Status st;

    private static final String NO_CONFIG;

    static{
        NO_CONFIG = "none";
    }

    private boolean parseConfig(String filename)
    {
        parameters = new ArrayList<>();
        Scanner scanner = null;
        try {
            File file = new File(filename);
            if (file.length()!= 0)
            {
                scanner = new Scanner(file);
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();
                    parameters.add(line);
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
        byte[] data = (byte[])indata;
        String b = "";
        String helpstring = new String(data);
        for (int i=0; i<helpstring.length(); i++)
        {
            if (Character.isLetter(helpstring.charAt(i)))
            {
                if (Character.isLowerCase(helpstring.charAt(i)))
                    b+=Character.toUpperCase(helpstring.charAt(i));
                else b+=Character.toLowerCase(helpstring.charAt(i));
            }
            else b+=helpstring.charAt(i);
        }
        data = b.getBytes();
        return data;
    }
    public FirstExecutor(Logger logger, String config)
    {
        log = logger;
        st = Status.OK;
        m_consumers = new ArrayList<>();
        m_producers = new ArrayList<>();
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
        m_consumers = new ArrayList<>();
        m_producers = new ArrayList<>();
        {
            if(!parseConfig(config)) {
                st = Status.EXECUTOR_ERROR;
                log.log("Got some problems with parsing executors config");
            }
        }
    }
    @Override
    public void loadDataFrom(Producer producer) {
        if(producer.status() == Status.OK) indata = producer.get();
    }
   /* @Override
    public void addProducers(List<Producer> producers){
        m_producers.addAll(producers);
    }
    @Override
    public void addProducer(Producer producer) {
        m_producers.add(producer);
    }
    @Override
    public void addConsumer(Consumer consumer) {
        m_consumers.add(consumer);
    }
    @Override
    public void addConsumers(List<Consumer> consumers){
        m_consumers.addAll(consumers);
    }*/
   @Override
   public void addProducers(List<Producer> producers){
       for (Producer i: producers)
       {
           if (i!=null) m_producers.add(i);
           else log.log("Tried to add null producer in executor");
       }
   }
    @Override
    public void addProducer(Producer producer) {
        if (producer!=null) m_producers.add(producer);
        else log.log("Tried to add null producer in executor");
    }
    @Override
    public void addConsumer(Consumer consumer) {
        if (consumer!=null) m_consumers.add(consumer);
        else log.log("Tried to add null consumer in executor");
    }
    @Override
    public void addConsumers(List<Consumer> consumers) {
        for (Consumer i : consumers) {
            if (i != null) m_consumers.add(i);
            else log.log("Tried to add null consumer in executor");
        }
    }
    @Override
    public Status status() {
        return this.st;
    }
    @Override
    public void run() {
        outdata = doMyJob();
        if(outdata == null) this.st = Status.EXECUTOR_ERROR;
        for (Consumer i: m_consumers)
        {
            i.loadDataFrom(this);
            i.run();
        }
    }
    @Override
    public Object get() {
        return outdata;
    }
}
