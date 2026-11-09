package com.minidb.model;

import java.io.Serializable;

public class Column implements Serializable{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final DataType type;
    private final boolean primaryKey;
    private final String refTable;
    private final String refColumn;

    public Column(String name, DataType type, boolean primaryKey) {
        this(name, type, primaryKey, null, null);
    }

    public Column(String name, DataType type, boolean primaryKey, String refTable, String refColumn) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.refTable = refTable;
        this.refColumn = refColumn;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getRefTable() {
        return refTable;
    }

    public String getRefColumn() {
        return refColumn;
    }

    public boolean hasForeignKey() {
        return refTable != null;
    }

    @Override
    public String toString() {
        return name + " " + type + (primaryKey ? "PRIMARY KEY" : "") + (hasForeignKey() ? "REFERENCES"  + refTable + "(" + refColumn + ")" : "");
    }
}
