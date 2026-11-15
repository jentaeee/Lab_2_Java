package com.minidb.parser.ast;
import com.minidb.query.Condition;

public class DeleteQuery {

    public final String table_name;
    public final Condition place;

    public DeleteQuery(String table_name, Condition place) {
        this.table_name = table_name;
        this.place = place;
    }
}