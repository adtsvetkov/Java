package com.company;

import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.Reader;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.Writer;
import ru.spbstu.pipeline.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MyManager implements Runnable{
    private MyLogger logger;
    private Status status = Status.OK;
    private List<Pairs> configdata;
    private List<Executor> executors = new ArrayList<>();
    private Reader reader;
    private Writer writer;

    public static final String Execute;
    public static final String Read;
    public static final String Write;

    static {
        Execute = MyManagerConfig.ConfigData.EXECUTOR.toString();
        Read = MyManagerConfig.ConfigData.READER.toString();
        Write = MyManagerConfig.ConfigData.WRITER.toString();
    }
    MyManager(MyLogger log, String filename) {
        this.logger = log;
        configdata = MyManagerConfig.getData(filename, logger);
        if (configdata == null) status = Status.ERROR;
        else createManagerData();
    }
    private boolean createManagerData()
    {
        for (int i=0; i<configdata.size(); i++)
        {
            Pairs elem = configdata.get(i);
            if (elem.getKey().equals(Execute))
            {
                 try
                 {
                     Class cls = Class.forName(elem.getValue());
                     Executor executor = (Executor)cls.getConstructor(String.class, Logger.class).newInstance(configdata.get(i+1).getValue(), logger.getUtilLogger());
                     executors.add(executor);
                 }
                 catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                     logger.writeToLogger(MyLogger.EXECUTOR_CONFIG);
                 }
            }
            if (elem.getKey().equals(Write)) writer = new MyWriter(logger.getUtilLogger(),elem.getValue());
            if (elem.getKey().equals(Read)) reader = new MyReader(logger.getUtilLogger(), elem.getValue());
        }
        return true;
    }
    @Override
    public void run()
    {
        if (!executors.isEmpty()) {
            reader.addConsumer(executors.get(0));
            executors.get(0).addProducer(reader);
            int thelast = executors.size() - 1;
            for (int i=0; i < thelast; i++) //тут добавляем эксекьюторы в очередь друг за другом
            {
                executors.get(i).addConsumer(executors.get(i + 1));
                executors.get(i+1).addProducer(executors.get(i));
            }
            executors.get(thelast).addConsumer(writer);
            writer.addProducer(executors.get(thelast));
        }
        else {
            reader.addConsumer(writer);
            writer.addProducer(reader);
        }
        if(((MyReader)reader).status() == Status.OK) reader.run();
        else
        {
            logger.writeToLogger("Reader initialization failed");
        }
        ((MyWriter)writer).closeStream();
    }
    public Status getStatus()
    {
        return status;
    }
}
