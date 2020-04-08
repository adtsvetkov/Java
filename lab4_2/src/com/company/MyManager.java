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
    private List<Thread> threads;

    public static final String Execute;
    public static final String Read;
    public static final String Write;
    public static final String Execute_num;

    static {
        Execute = MyManagerConfig.ConfigData.EXECUTOR.toString();
        Read = MyManagerConfig.ConfigData.READER.toString();
        Write = MyManagerConfig.ConfigData.WRITER.toString();
        Execute_num = MyManagerConfig.ConfigData.EXECUTORSNUMBER.toString();
    }
    MyManager(MyLogger log, String filename) {
        this.logger = log;
        list_executors = new ArrayList<>();
        configdata = MyManagerConfig.getData(filename, logger);
        if (configdata == null) status = Status.ERROR;
        else createManagerData();
    }
    private boolean createManagerData()
    {
        Pairs elem;
        int flag = 0;
        for (int i = 0; i < configdata.size(); i++)
        {
            elem = configdata.get(i);
            if (elem.getKey().equals(Execute_num)) {
                try {
                    list_executors.add(new ArrayList<>());
                    int executorsnumber = Integer.parseInt(elem.getValue());
                    for (int j = 1; j <= 2*executorsnumber; j++) {
                        elem = configdata.get(i+j); //берем следующий элемент
                        if (elem.getKey().equals(Execute)) {
                            try {
                                Class cls = Class.forName(elem.getValue());
                                Executor executor = (Executor) cls.getConstructor(String.class, Logger.class).newInstance(configdata.get(i + j + 1).getValue(), logger.getUtilLogger());
                                list_executors.get(flag).add(executor);
                            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                logger.writeToLogger(MyLogger.EXECUTOR_CONFIG);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    logger.writeToLogger("Something went wrong with threads");
                    return false;
                }
                flag++;
            }
            if (elem.getKey().equals(Write)) writer = new MyWriter(logger.getUtilLogger(), elem.getValue());
            if (elem.getKey().equals(Read)) reader = new MyReader(logger.getUtilLogger(), elem.getValue());
        }
        return true;
    }
    @Override
    public void run()
    {
        threads = new ArrayList<>();
        boolean st = true;
        Thread readthread = new Thread(reader); //создаем тред дл€ ридера
        threads.add(readthread);
        if (((MyReader)reader).status() != Status.OK) st = false;
        if (!list_executors.isEmpty()) {
                for (int j = 0; j < list_executors.size(); j++) {
                    reader.addConsumer(list_executors.get(j).get(0));
                    Thread thr0 = new Thread(list_executors.get(j).get(0)); //тред дл€ первого эксекутора
                    threads.add(thr0);
                    if (list_executors.get(j).get(0).status() != Status.OK) st = false;
                    list_executors.get(j).get(0).addProducer(reader);
                    int thelast = list_executors.get(j).size() - 1;
                    for (int i = 0; i < thelast; i++) //тут добавл€ем эксекьюторы в очередь друг за другом
                    {
                        list_executors.get(j).get(i).addConsumer(list_executors.get(j).get(i + 1));
                        list_executors.get(j).get(i + 1).addProducer(list_executors.get(j).get(i));
                        Thread thr1 = new Thread(list_executors.get(j).get(i + 1)); //кидаем треды дл€ цепочки эксекуторов
                        threads.add(thr1);
                        if (list_executors.get(j).get(i + 1).status() != Status.OK) st = false;
                    }
                    list_executors.get(j).get(thelast).addConsumer(writer);
                    writer.addProducer(list_executors.get(j).get(thelast));
                    if (list_executors.get(j).get(thelast).status() != Status.OK) st = false;
                }
        }
        else {
            reader.addConsumer(writer); //знакомим работников
            writer.addProducer(reader);
        }
        Thread writethread = new Thread(writer); //тред дл€ райтера
        threads.add(writethread);
        if (writer.status() != Status.OK) st = false;
        if (st) {
            for (Thread t : threads) { //запускаем треды в пор€дке добавлени€ в лист
                t.start();
            }
        }
        else
        {
            logger.writeToLogger("Transporter initialization failed");
        }
    }
    public Status getStatus()
    {
        return status;
    }
}
