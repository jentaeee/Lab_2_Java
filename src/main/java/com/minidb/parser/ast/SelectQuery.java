package com.minidb.parser.ast;
import java.util.*;
import com.minidb.query.Condition;

public class SelectQuery {

    public final String table_name;
    public final List<String> cols;
    public final Condition place;

    public  SelectQuery(String table_name, List<String> cols, Condition place) {
        this.table_name = table_name;
        this.cols = cols;
        this.place = place;
    }
}