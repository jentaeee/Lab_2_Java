package com.minidb.query;

import java.util.Collections;
import java.util.List;

public class QueryResult {
    private final List<String> columns;
    private final List<List<Object>> rows;
    private final boolean usedIndex;

    public QueryResult(List<String> columns, List<List<Object>> rows, boolean usedIndex) {
        this.columns = columns;
        this.rows = rows;
        this.usedIndex = usedIndex;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public boolean isUsedIndex() {
        return usedIndex;
    }

    public int size() {
        return rows.size();
    }
}
