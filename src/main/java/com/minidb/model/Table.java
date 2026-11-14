package com.minidb.model;

import com.minidb.exception.DbException;
import com.minidb.index.HashIndex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<Column> columns;
    private final int pkPosition;
    private final Map<Object, Row> rows = new LinkedHashMap<>();
    private final Map<String, HashIndex> indexes = new LinkedHashMap<>();

    public Table(String name, List<Column> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new DbException("В таблице должен быть хотя бы один столбец");
        }
        Set<String> names = new HashSet<>();
        int pk = -1;
        for (int i = 0; i < columns.size(); i++) {
            Column c = columns.get(i);
            if (!names.add(c.getName().toLowerCase())) {
                throw new DbException("Повторяется имя столбца: " + c.getName());
            }
            if (c.isPrimaryKey()) {
                if (pk != -1) {
                    throw new DbException("Первичный ключ должен быть один");
                }
                pk = i;
            }
        }
        if (pk == -1) {
            throw new DbException("В таблице нет первичного ключа");
        }
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.pkPosition = pk;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public Column column(int position) {
        return columns.get(position);
    }

    public int getPkPosition() {
        return pkPosition;
    }

    public int columnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new DbException("Столбец '" + columnName + "' не найден в таблице " + name);
    }

    public int size() {
        return rows.size();
    }

    public Collection<Row> allRows() {
        return Collections.unmodifiableCollection(rows.values());
    }

    public boolean containsPk(Object pk) {
        return rows.containsKey(pk);
    }

    public Row findByPk(Object pk) {
        return rows.get(pk);
    }

    public void insertRow(Row row) {
        Object pk = row.get(pkPosition);
        if (pk == null) {
            throw new DbException("Первичный ключ не может быть NULL");
        }
        if (rows.containsKey(pk)) {
            throw new DbException("Нарушение уникальности первичного ключа: " + pk);
        }
        rows.put(pk, row);
        for (HashIndex index : indexes.values()) {
            index.add(row.get(index.getColumnPos()), pk);
        }
    }

    public void removeRow(Row row) {
        Object pk = row.get(pkPosition);
        rows.remove(pk);
        for (HashIndex index : indexes.values()) {
            index.remove(row.get(index.getColumnPos()), pk);
        }
    }

    public void setValue(Row row, int position, Object newValue) {
        Object pk = row.get(pkPosition);
        Object oldValue = row.get(position);
        for (HashIndex index : indexes.values()) {
            if (index.getColumnPos() == position) {
                index.remove(oldValue, pk);
                index.add(newValue, pk);
            }
        }
        row.set(position, newValue);
    }

    public void addIndex(String indexName, String columnName) {
        String key = indexName.toLowerCase();
        if (indexes.containsKey(key)) {
            throw new DbException("Индекс уже существует: " + indexName);
        }
        int position = columnIndex(columnName);
        if (findIndexByColumn(position) != null) {
            throw new DbException("По столбцу " + columnName + " индекс уже есть");
        }
        HashIndex index = new HashIndex(indexName, columns.get(position).getName(), position);
        for (Row row : rows.values()) {
            index.add(row.get(position), row.get(pkPosition));
        }
        indexes.put(key, index);
    }

    public HashIndex findIndexByColumn(int position) {
        for (HashIndex index : indexes.values()) {
            if (index.getColumnPos() == position) {
                return index;
            }
        }
        return null;
    }

    public Collection<HashIndex> getIndexes() {
        return Collections.unmodifiableCollection(indexes.values());
    }
}