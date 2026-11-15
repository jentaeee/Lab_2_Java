package com.minidb.parser.ast;

public class CreateIndexQuery {
    public final String index_name;
    public final String table_name;
    public final String col_name;

    public CreateIndexQuery(String index_name, String table_name, String col_name) {
        this.index_name = index_name;
        this.table_name = table_name;
        this.col_name = col_name;
    }
}