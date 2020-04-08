package com.company;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) MyLogger.writeToLogger(MyLogger.WRONG_ARGUMENTS); //если не два аргумента
        else {
            if (MyConfig.CreateMyConfig(args[0])) { //создаем конфиг
                if (MyLogger.createLogger(MyConfig.getLog())) { //если логгер создался
                    if (MyConfig.getAction().equals(MyConfig.Compress)) { //если ACTION = compress
                        Compress.compress(MyConfig.getInput(), MyConfig.getOutput());
                        MyLogger.writeToLogger(MyLogger.SUCCESS);
                    } else if (MyConfig.getAction().equals(MyConfig.Decompress)) { //если ACTION = decompress
                        if(Decompress.decompress(MyConfig.getInput(), MyConfig.getOutput())) MyLogger.writeToLogger(MyLogger.SUCCESS);
                    } else MyLogger.writeToLogger(MyLogger.CONFIG); //если непонятно че за экшн
                }
            }
        }
    }
}