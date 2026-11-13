package com.minidb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Row implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Object[] values;

    public Row(Object[] values) {
        this.values = values;
    }

    public Object get(int position) {
        return values[position];
    }

    public void set(int position, Object value) {
        values[position] = value;
    }

    public List<Object> toList() {
        return new ArrayList<>(Arrays.asList(values));
    }
}
