package com.company.mypipeline;

import java.util.ArrayList;
import java.util.List;

import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

//заменяет первый символ блока на #

//этому эксекьютору не нужен конфиг

public class ThirdExecutor implements Executor {
    private ArrayList<Consumer> m_consumers;
    private ArrayList<Producer> m_producers;
    private Object indata;
    private Object outdata;

    private Logger log;
    private Status st;

    private static final String NO_CONFIG;
    private static final byte Hashtag;

    static{
        NO_CONFIG = "none";
        Hashtag = '#';
    }

    private Object doMyJob()
    {
        byte[] data = (byte[])indata;
        data[0] = Hashtag;
        return data;
    }
    public ThirdExecutor(Logger logger, String config)
    {
        log = logger;
        st = Status.OK;
        m_consumers = new ArrayList<>();
        m_producers = new ArrayList<>();
        if (!config.toLowerCase().equals(NO_CONFIG))
            {
                this.st = Status.EXECUTOR_ERROR;
                log.log("Got some problems with parsing executors config");
            }

    }
    public ThirdExecutor(String config, Logger logger)
    {
        log = logger;
        st = Status.OK;
        m_consumers = new ArrayList<>();
        m_producers = new ArrayList<>();
        if (!config.toLowerCase().equals(NO_CONFIG))
        {
            this.st = Status.EXECUTOR_ERROR;
            log.log("Got some problems with parsing executors config");
        }
    }
    @Override
    public void loadDataFrom(Producer producer) {
        if(producer.status() == Status.OK) indata = producer.get();
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
    public Object get() {
        return outdata;
    }
}
