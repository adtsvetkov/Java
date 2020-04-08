package com.company.mypipeline;

import ru.spbstu.pipeline.Status;

public class Main {

    public static void main(String[] args) {
        MyLogger myLogger = new MyLogger();
        if(myLogger.status == Status.OK)
        {
            if (args.length != 1) myLogger.writeToLogger(MyLogger.WRONG_ARGUMENTS); //если не один аргумент
            else {
                MyManager manager = new MyManager(myLogger, args[0]);
                if (manager.getStatus().toString().equals(Status.OK.toString()))
                {
                    manager.run();
                }
             }
        }
    }
}
