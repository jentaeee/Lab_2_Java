package com.minidb.db;

import com.minidb.model.Column;
import com.minidb.query.Condition;
import com.minidb.query.QueryResult;

import java.util.List;
import java.util.Map;

/** Контракт СУБД: именно эти методы вызывает парсер запросов. */
public interface Database {

    /** CREATE TABLE. Ровно один столбец должен быть первичным ключом. */
    void createTable(String tableName, List<Column> columns);

    /** DROP TABLE. */
    void dropTable(String tableName);

    /**
     * INSERT INTO. Вставляет одну или несколько записей (всё или ничего).
     *
     * @param columns имена столбцов; null или пустой список = все столбцы по порядку
     * @param rows    записи: каждая запись - список значений
     * @return количество вставленных записей
     */
    int insert(String tableName, List<String> columns, List<List<Object>> rows);

    /**
     * SELECT.
     *
     * @param columns null, пустой список или ["*"] = все столбцы
     * @param where   условие или null
     */
    QueryResult select(String tableName, List<String> columns, Condition where);

    /** UPDATE ... SET ... WHERE. Возвращает количество изменённых записей. */
    int update(String tableName, Map<String, Object> assignments, Condition where);

    /** DELETE ... WHERE. Возвращает количество удалённых записей. */
    int delete(String tableName, Condition where);

    /** CREATE INDEX индекс ON таблица (столбец). */
    void createIndex(String indexName, String tableName, String columnName);

    /** Названия всех таблиц. */
    List<String> listTables();

    /** Описание столбцов таблицы. */
    List<Column> describeTable(String tableName);
}