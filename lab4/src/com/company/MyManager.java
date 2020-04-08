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
    private Reader reader;
    private Writer writer;
    private List<List<Executor>> list_executors;
    private int threadnumber;

    public static final String Execute;
    public static final String Read;
    public static final String Write;
    public static final String Thread_num;

    public static final int maxthreadnumber = 10;

    private int findIndex(List<Pairs> kek, String value)
    {
        for (int i = 0; i<kek.size(); i++)
        {
            if (kek.get(i).getKey().equals(value)) return i;
        }
        return -1;
    }
    private static final int invalidthreadnumber = -1;

    static {
        Execute = MyManagerConfig.ConfigData.EXECUTOR.toString();
        Read = MyManagerConfig.ConfigData.READER.toString();
        Write = MyManagerConfig.ConfigData.WRITER.toString();
        Thread_num = MyManagerConfig.ConfigData.THREADNUMBER.toString();
    }
    MyManager(MyLogger log, String filename) {
        this.logger = log;
        threadnumber = invalidthreadnumber;
        list_executors = new ArrayList<>();
        configdata = MyManagerConfig.getData(filename, logger);
        list_executors = new ArrayList<>();
        if (configdata == null) status = Status.ERROR;
        else createManagerData();
    }
    private boolean createManagerData()
    {
        //поскольку задача стоит оптимизировать скорость вычислений, эксекуторы для потоков одинаковые
        Pairs elem;
        int index = findIndex(configdata, Thread_num);
        if (index!=-1) {
            try {
                String kek = configdata.get(index).getValue();
                threadnumber = Integer.parseInt(kek);
                for (int i = 0; i<threadnumber; i++)
                {
                    list_executors.add(new ArrayList<>());
                }
            }catch (NullPointerException e){
                logger.writeToLogger("Something went wrong with threads");
                return false;
            }
        }
        else
        {
            logger.writeToLogger("Something went wrong with threads");
            return false;
        }
        if (threadnumber <0 || threadnumber>maxthreadnumber) threadnumber = maxthreadnumber;
        for (int i=0; i<configdata.size(); i++)
        {
            elem = configdata.get(i);
            if (elem.getKey().equals(Execute)) {
                for (int j=0; j<threadnumber; j++) {
                    try {
                        Class cls = Class.forName(elem.getValue());
                        Executor executor = (Executor) cls.getConstructor(String.class, Logger.class).newInstance(configdata.get(i + 1).getValue(), logger.getUtilLogger());
                        list_executors.get(j).add(executor);
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        logger.writeToLogger(MyLogger.EXECUTOR_CONFIG);
                    }
                }
            }
            if (elem.getKey().equals(Write)) writer = new MyWriter(logger.getUtilLogger(), elem.getValue());
            if (elem.getKey().equals(Read)) reader = new MyReader(logger.getUtilLogger(), elem.getValue());
        }
        return true;
    }
    @Override
    public void run()
    {
        if (!list_executors.isEmpty()) {
            for (int j = 0; j<threadnumber; j++)
            {
                reader.addConsumer(list_executors.get(j).get(0));
                list_executors.get(j).get(0).addProducer(reader);
                int thelast = list_executors.get(j).size() - 1;
                for (int i = 0; i < thelast; i++) //тут добавляем эксекьюторы в очередь друг за другом
                {
                    list_executors.get(j).get(i).addConsumer(list_executors.get(j).get(i + 1));
                    list_executors.get(j).get(i + 1).addProducer(list_executors.get(j).get(i));
                }
                list_executors.get(j).get(thelast).addConsumer(writer);
                writer.addProducer(list_executors.get(j).get(thelast));
            }
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
