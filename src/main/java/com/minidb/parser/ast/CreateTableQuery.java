package com.minidb.parser.ast;
import com.minidb.model.Column;
import java.util.*;

public class CreateTableQuery {
    public final String table_name;
    public final List<Column> cols;

    public CreateTableQuery(String table_name, List<Column> cols) {
        this.table_name = table_name;
        this.cols = cols;
    }
}