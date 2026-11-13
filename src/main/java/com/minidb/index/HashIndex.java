package com.minidb.index;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HashIndex implements Serializable{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String columnName;
    private final int columnPos;
    private final Map<Object, Set<Object>> map = new HashMap<>();

    public HashIndex(String name, String columnName, int columnPos) {
        this.name = name;
        this.columnName = columnName;
        this.columnPos = columnPos;
    }

    public String getName() {
        return name;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnPos() {
        return columnPos;
    }

    public void add(Object value, Object primaryKey) {
        map.computeIfAbsent(value, k -> new LinkedHashSet<>()).add(primaryKey);
    }

    public void remove(Object value, Object primaryKey) {
        Set <Object> keys = map.get(value);
        if (keys != null) {
            keys.remove(primaryKey);
            if (keys.isEmpty()) {
                map.remove(value);
            }
        }
    }

    public Set<Object> find(Object value) {
        Set<Object> keys = map.get(value);
        return keys == null ? Collections.<Object>emptySet() : keys;
    }
}
