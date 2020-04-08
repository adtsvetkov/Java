package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Decompress {
    public static boolean isValid(String pair[]) {
        boolean flag = false;
        try {
            Double value = Double.parseDouble(pair[1]);
            if ((pair.length == HelpClass.CorrectFileLen) && (pair[0].length() == HelpClass.CorrectSymLen) && (value > HelpClass.LeftValue) && (value <= HelpClass.RightValue)) flag = true;
        } catch (Exception e) //неправильные числа
        {
            MyLogger.writeToLogger(MyLogger.INPUT_FILE_INVALID);
        }
        return flag;
    }

    public static char findCharacter(List<Pairs> pairs, Double value) //binary search
    {
        int left = -1;
        int right = pairs.size();
        Double leftvalue = HelpClass.LeftValue;
        Double rightvalue = HelpClass.LeftValue;
        int middle = 0;
        while (!((value > leftvalue) && (value < rightvalue))) {
            middle = (left + right) / 2;
            if (middle > 0) leftvalue = pairs.get(middle - 1).getValue();
            else leftvalue = HelpClass.LeftValue;
            rightvalue = pairs.get(middle).getValue();
            if (value < leftvalue) right = middle;
            else if (value >= rightvalue) left = middle;
        }
        return pairs.get(middle).getKey();
    }

    public static boolean decode(DecodeClass decodeclass, String filename) {
        String ans = HelpClass.EmptyString;
        Double statement = decodeclass.getAns();
        List<Pairs> pairs = decodeclass.getPairs();
        Double l, h; //low and high
        int k;
        char character;
        do {
            character = findCharacter(pairs, statement);
            if (character != HelpClass.EndOfMessage) ans += character;
            k = HelpClass.findIndex(pairs, character);
            if (k == -1)
            {
                MyLogger.writeToLogger(MyLogger.ARITHMETICAL_ERROR);
                return false;
            }
            if (k > 0) l = pairs.get(k - 1).getValue();
            else l = HelpClass.LeftValue;
            h = pairs.get(k).getValue();
            if ((h - l) != 0) statement = (statement - l) / (h - l);
            else
            {
                MyLogger.writeToLogger(MyLogger.ARITHMETICAL_ERROR);
                return false;
            }
        } while (character != HelpClass.EndOfMessage);
        FileWriter fw = null;
        boolean flag = true;
        try {
            File file = new File(filename);
            if (!file.exists()) flag = false;
            fw = new FileWriter(file, flag);
            fw.write(ans);
            fw.append("\r\n");
            fw.flush();
        } catch (IOException kek) //unable to open for writing
        {
            MyLogger.writeToLogger(MyLogger.FILE_WRITER);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException file_not_found) {
                MyLogger.writeToLogger(MyLogger.FILE_WRITER);
            }
        }
        return true;
    }

    public static DecodeClass getDatadecode(Scanner scanner) {
        String line;
        DecodeClass returnelem = new DecodeClass();
        List<Pairs> pairs = new ArrayList<>();
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.isEmpty()) break;
            line = line.replaceAll(HelpClass.Comma, HelpClass.Dot);
            String[] pair = line.split(HelpClass.Semicolon);
            Pairs elem = new Pairs();
            if (isValid(pair)) elem.SetKey(pair[0]);
            else {
                MyLogger.writeToLogger(MyLogger.INPUT_FILE_INVALID);
                return returnelem;
            }
            elem.SetValue(Double.parseDouble(pair[1]));
            pairs.add(elem);
        }
        if (!pairs.isEmpty()) returnelem.setPairs(pairs);
        else {
            MyLogger.writeToLogger(MyLogger.INPUT_FILE_INVALID);
            return returnelem;
        }
        line = scanner.nextLine();
        line = line.replaceAll(HelpClass.Comma, HelpClass.Dot);
        line = line.replace(HelpClass.Space, HelpClass.EmptyString);
        returnelem.setAns(Double.parseDouble(line));
        return returnelem;
    }

    public static boolean decompress(String filename, String filename2) {
        Scanner scanner = null;
        try {
            File file = new File(filename);
            if (file.length() != 0) {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    DecodeClass dc = getDatadecode(scanner);
                    if (dc.getAns() != null && dc.getPairs() != null)
                    {
                        if(!decode(dc, filename2)) return false;
                    }
                    else return false;
                }
            } else {
                MyLogger.writeToLogger(MyLogger.INPUT_FILE_EMPTY);
                return false;
            }
        } catch (FileNotFoundException file_not_found) //file cannot be opened
        {
            MyLogger.writeToLogger(MyLogger.INPUT_FILE_OPEN);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return true;
    }
}
