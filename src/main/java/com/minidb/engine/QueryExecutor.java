package com.minidb.engine;
import com.minidb.db.Database;
import com.minidb.parser.ast.*;

public class QueryExecutor {

    private final Database database;

    public QueryExecutor(Database database) {
        this.database = database;
    }

    public Object processQuery(Object query) {
        if (query instanceof InsertQuery) {
            InsertQuery q = (InsertQuery) query;
            int count = database.insert(q.table_name, q.cols, q.rows);
            return "Вставлено записей: " + count;
        } 

        if (query instanceof SelectQuery) {
            SelectQuery q = (SelectQuery) query;
            return database.select(q.table_name, q.cols, q.place); 
        } 

        if (query instanceof UpdateQuery) {
            UpdateQuery q = (UpdateQuery) query;
            int count = database.update(q.table_name, q.assign, q.place);
            return "Обновлено записей: " + count;
        } 

        if (query instanceof DeleteQuery) {
            DeleteQuery q = (DeleteQuery) query;
            int count = database.delete(q.table_name, q.place);
            return "Удалено записей: " + count;
        }

        if (query instanceof CreateTableQuery) {
            CreateTableQuery q = (CreateTableQuery) query;
            database.createTable(q.table_name, q.cols);
            return "Таблица " + q.table_name + " создана";
        }

        if (query instanceof DropTableQuery) {
            DropTableQuery q = (DropTableQuery) query;
            database.dropTable(q.table_name);
            return "Таблица " + q.table_name + " удалена";
        }

        if (query instanceof CreateIndexQuery) {
            CreateIndexQuery q = (CreateIndexQuery) query;
            database.createIndex(q.index_name, q.table_name, q.col_name);
            return "Индекс " + q.index_name + " создан";
        }

        throw new UnsupportedOperationException("Неизвестный запрос: " + query.getClass().getSimpleName());
    }
}