package com.minidb.storage;

import com.minidb.model.Table;

import java.util.List;


public interface TableStorage {
    void save(Table table);

    List<Table> loadAll();

    void delete(String tableName);
}
