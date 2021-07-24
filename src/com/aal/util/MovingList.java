package com.aal.util;

import java.util.ArrayList;

public class MovingList<T> {
    ArrayList<T> data;
    int size;

    public MovingList(int size) {
        data = new ArrayList<>(size);
        this.size = size;
    }

    public void add(T element) {
        if (isReady())
            data.remove(0);
        data.add(element);
    }

    public void setLast(T element) {
        data.set(data.size() - 1, element);
    }

    public void clear() {
        data.clear();
    }

    public boolean isReady() {
        return data.size() == size;
    }

    public ArrayList<T> getData() {
        return data;
    }
}
