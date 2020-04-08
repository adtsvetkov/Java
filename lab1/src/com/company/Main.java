package com.company;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) MyLogger.writeToLogger(MyLogger.WRONG_ARGUMENTS); //���� �� ��� ���������
        else {
            if (MyConfig.CreateMyConfig(args[0])) { //������� ������
                if (MyLogger.createLogger(MyConfig.getLog())) { //���� ������ ��������
                    if (MyConfig.getAction().equals(MyConfig.Compress)) { //���� ACTION = compress
                        Compress.compress(MyConfig.getInput(), MyConfig.getOutput());
                        MyLogger.writeToLogger(MyLogger.SUCCESS);
                    } else if (MyConfig.getAction().equals(MyConfig.Decompress)) { //���� ACTION = decompress
                        if(Decompress.decompress(MyConfig.getInput(), MyConfig.getOutput())) MyLogger.writeToLogger(MyLogger.SUCCESS);
                    } else MyLogger.writeToLogger(MyLogger.CONFIG); //���� ��������� �� �� ����
                }
            }
        }
    }
}