package com.minidb.db;
//заготовка
import com.minidb.model.Column;
import com.minidb.query.Condition;
import com.minidb.query.QueryResult;

import java.util.List;
import java.util.Map;

public interface DataBase {
    void createTable(String tableName, List<Column> columns);

    void dropTable(String tableName);

    int insert(String tableName, List<String> columns, Condition where);

    int update(String tableName, Map<String, Object> assignments, Condition where);

    void createIndex(String indexName, String tableName, String columnName);

    List<String> listTables();

    List<Column> describeTable(String tableName);
}
