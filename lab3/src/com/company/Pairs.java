package com.company;

public class Pairs {
    private String value;
    private String key;
    public void setValues(String k, String v)
    {
        value = v;
        key = k;
    }
    public String getValue()
    {
        return value;
    }
    public String getKey()
    {
        return key;
    }
    public void SetKey(String k)
    {
        key = k;
    }
    public void SetValue(String v)
    {
        value = v;
    }
}
