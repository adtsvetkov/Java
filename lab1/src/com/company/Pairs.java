package com.company;

//массив пар - буква/значение интервала в котором она лежит (сортировка по частоте, затем помещаем в интервал от 0 до 1 в зависимости от того как часто встречается)
public class Pairs {
    private Double value;
    private char key;
    public void setValues(Double v, char k)
    {
        value = v;
        key = k;
    }
    public void setValues(Double v, String k)
    {
        value = v;
        key = k.charAt(0);
    }
    public Double getValue()
    {
        return value;
    }
    public char getKey()
    {
        return key;
    }
    public void SetKey(char k)
    {
        key = k;
    }
    public void SetKey(String k)
    {
        key = k.charAt(0);
    }
    public void SetValue(Double v)
    {
        value = v;
    }
}
