package com.company;

import java.io.*;
import java.util.*;

public class Compress {
    public static boolean isSorted(List<Pairs> kek) {
        for (int i = 1; i < kek.size(); i++) {
            if (kek.get(i).getValue() > kek.get(i - 1).getValue()) return false;
        }
        return true;
    }

    public static boolean code(List<Pairs> pairs, String filename, String line) {
        Double l_old = HelpClass.LeftValue, h_old = HelpClass.RightValue;
        for (int i = 0; i < line.length(); i++) {
            char k = line.charAt(i); //����� ������ ������
            int index = HelpClass.findIndex(pairs, k);
            if (index == -1) {
                MyLogger.writeToLogger(MyLogger.LINE_SYMBOLS);
                return false;
            }
            Double v = pairs.get(index).getValue(); //����� �������� ��� ������ �������
            Double l_sym, h_sym; //[L,H] - �������
            if (index == 0) l_sym = HelpClass.LeftValue;
            else l_sym = pairs.get(index - 1).getValue();
            h_sym = v;
            if (i == 0) {
                l_old = l_sym;
                h_old = h_sym;
            } else {
                Double l = l_old;
                Double m = (h_old - l_old);
                h_old = l + m * h_sym;
                l_old = l + m * l_sym;
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(filename, true); //���� ���������� ���� �� �������� true, ����� false
            Double ans = l_old + (h_old - l_old) / 2;
            String s = String.format("%.16f", ans);
            fw.write(s);
            fw.write("\r\n");
        } catch (IOException file_not_found) {
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

    private static List<Pairs> getDatacode(String line, String filename) {
        HashMap<String, Double> pairs = new HashMap<>(); //������ ��� ���, ����� ���������
        for (int i = 0; i < line.length(); i++) //����������� �� ���� ��������� ������, ���������� �� � ��� � ����������� ����������
        {
            char character = line.charAt(i);
            if (!pairs.containsKey(String.valueOf(character))) {
                pairs.put(String.valueOf(character), 1.0);
            } else {
                Double k = pairs.get(String.valueOf(character));
                k += 1;
                pairs.put(String.valueOf(character), k);
            }
        }
        double size = 1.0 / line.length();
        List<Pairs> newpairs = new ArrayList<>();
        for (Map.Entry<String, Double> maps : pairs.entrySet()) //�������������� � ���� �� ����, ����������� ������� � ����
        {
            Double value = maps.getValue();
            value *= size;
            Pairs kek = new Pairs();
            kek.setValues(value, maps.getKey());
            newpairs.add(kek);
        }
        Collections.sort(newpairs, (Pairs o1, Pairs o2) -> {//��� ������ �������
            return -o1.getValue().compareTo(o2.getValue());
        });
        if (!isSorted(newpairs)) {
            MyLogger.writeToLogger(MyLogger.SORTING_FUNCTION);
            return null;
        }
        FileWriter fw = null;
        boolean flag = true;
        try {
            File file = new File(filename);
            if (!file.exists()) flag = false;
            fw = new FileWriter(file, flag);
            for (Pairs i : newpairs) //�������������� � ������ ��� ���������� � ������� ��� ����� � ������ � ����
            {
                String k = Character.toString(i.getKey());
                fw.write(k);
                if (newpairs.indexOf(i) != 0) {
                    int index = newpairs.indexOf(i) - 1;
                    Double newvalue = i.getValue() + newpairs.get(index).getValue();
                    i.setValues(newvalue, i.getKey());
                }
                if (newpairs.indexOf(i) == newpairs.size() - 1) i.setValues(1.0, i.getKey());
                Double v = i.getValue();
                String s = String.format("%.16f", v);
                fw.write(";");
                fw.write(s);
                fw.write("\r\n");
            }
            fw.append("\r\n");
            fw.flush();
        } catch (IOException file_not_found) {
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
        return newpairs;
    }

    public static boolean compress(String filename, String filename2) {
        BufferedReader reader = null;
        try {
            File file = new File(filename);
            if (file.length() != 0) {
                FileReader fr = new FileReader(file);
                reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.equals(HelpClass.EmptyString)) {
                        line += HelpClass.EndOfMessage;
                        List<Pairs> list = getDatacode(line, filename2);
                        if (list != null)
                        {
                            if (!code(list, filename2, line)) return false;
                        }
                        else return false;
                    }
                }
            } else {
                MyLogger.writeToLogger(MyLogger.INPUT_FILE_EMPTY);
                return false;
            }
        } catch (FileNotFoundException file_not_found) {
            MyLogger.writeToLogger(MyLogger.INPUT_FILE_OPEN);
        } catch (IOException line_is_empty)
        {
            MyLogger.writeToLogger(MyLogger.LINE_EMPTY);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException reader_err) {
                MyLogger.writeToLogger(MyLogger.BUFFERED_READER);
            }
        }
        return true;
    }
}
