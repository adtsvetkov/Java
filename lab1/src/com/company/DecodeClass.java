package com.company;

import java.util.List;

//класс для функции декодирования - чисто чтобы функция его возвращала, содержит массив пар и закодированное число
public class DecodeClass {
    private Double answer;
    private List<Pairs> pairs;

    public DecodeClass()
    {
        answer = null;
        pairs = null;
    }
    public void setAns(Double a) {
        answer = a;
    }

    public void setPairs(List<Pairs> p) {
        pairs = p;
    }

    public Double getAns() {
        return answer;
    }
    public List<Pairs> getPairs()
    {
        return pairs;
    }
}
