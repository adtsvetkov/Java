package com.company;

import ru.spbstu.pipeline.Producer;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Triplet {
    private Producer producer;
    private Producer.DataAccessor dataAccessor;
    private Queue<Object> data = new ArrayBlockingQueue<>(1); //это блокирующая очередь из одного элемента для данных из ридера
    public void setElems(Producer prod, Producer.DataAccessor dataAcc, Object data)
    {
        producer = prod;
        dataAccessor = dataAcc;
        this.data.add(data);
    }
    public void setElems(Producer prod, Producer.DataAccessor dataAcc)
    {
        producer = prod;
        dataAccessor = dataAcc;
    }
    public Producer getProducer() {
        return producer;
    }
    public Producer.DataAccessor getDataAccessor()
    {
        return dataAccessor;
    }
    public void setData(Object data)
    {
        this.data.add(data);
    }
    public Queue<Object> getData()
    {
        return data;
    }
}
