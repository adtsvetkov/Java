package com.company;

import java.util.List;

public class HelpClass {
    public static final char EndOfMessage;
    public static final String EmptyString;
    public static final String Comma;
    public static final String Dot;
    public static final String Semicolon;
    public static final String Space;
    public static final String Error;
    public static final int CorrectFileLen;
    public static final int CorrectSymLen;
    public static final Double LeftValue;
    public static final Double RightValue;


    static
    {
        EndOfMessage = '#';
        EmptyString = "";
        Comma = ",";
        Dot = ".";
        Semicolon = ";";
        Space = " ";
        Error = "Error logger.txt";
        CorrectFileLen = 2;
        CorrectSymLen = 1;
        LeftValue = 0.0;
        RightValue = 1.0;
    }

    public static int findIndex(List<Pairs> pairs, char k){
        if (!pairs.isEmpty()) {
            for (int i = 0; i < pairs.size(); i++) {
                if (pairs.get(i).getKey() == k) return i;
            }
        }
        return -1;
    }
}
