package com.minidb.parser.ast;
import java.util.*;

public class InsertQuery {
    
    public final String table_name;
    public final List<String> cols;
    public final List<List<Object>> rows;

    public InsertQuery(String table_name, List<String> cols, List<List<Object>> rows) {
        this.table_name = table_name;
        this.cols = cols;
        this.rows = rows;
    }
}