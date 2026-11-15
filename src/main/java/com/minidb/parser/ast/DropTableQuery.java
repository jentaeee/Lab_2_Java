package com.minidb.parser.ast;

public class DropTableQuery {
    public final String table_name;

    public DropTableQuery(String table_name) {
        this.table_name = table_name;
    }
}