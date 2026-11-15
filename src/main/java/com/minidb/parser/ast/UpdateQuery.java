package com.minidb.parser.ast;
import java.util.*;
import com.minidb.query.Condition;

public class UpdateQuery {

    public final String table_name;
    public final Map<String, Object> assign;
    public final Condition place;
    
    public UpdateQuery(String table_name, Map<String, Object> assign, Condition place) {
        this.table_name = table_name;
        this.assign = assign;
        this.place = place;
    }
}
