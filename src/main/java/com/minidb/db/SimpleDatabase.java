package com.minidb.db;

import com.minidb.exception.DbException;
import com.minidb.index.HashIndex;
import com.minidb.model.Column;
import com.minidb.model.Row;
import com.minidb.model.Table;
import com.minidb.query.Condition;
import com.minidb.query.Operator;
import com.minidb.query.QueryResult;
import com.minidb.storage.TableStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class SimpleDatabase implements Database {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final TableStorage storage;
    private final Map<String, Table> tables = new LinkedHashMap<>();


    public SimpleDatabase(TableStorage storage) {
        this.storage = storage;
        for (Table table : storage.loadAll()) {
            tables.put(key(table.getName()), table);
        }
    }

    @Override
    public void createTable(String tableName, List<Column> columns) {
        checkIdentifier(tableName);
        if (tables.containsKey(key(tableName))) {
            throw new DbException("Таблица уже существует: " + tableName);
        }
        for (Column column : columns) {
            checkIdentifier(column.getName());
            if (column.hasForeignKey()) {
                checkForeignKeyDefinition(column);
            }
        }
        Table table = new Table(tableName, columns);
        tables.put(key(tableName), table);
        storage.save(table);
    }

    @Override
    public void dropTable(String tableName) {
        Table table = table(tableName);
        for (Table other : tables.values()) {
            if (other == table) {
                continue;
            }
            for (Column column : other.getColumns()) {
                if (column.hasForeignKey() && column.getRefTable().equalsIgnoreCase(table.getName())) {
                    throw new DbException("Таблицу " + tableName
                            + " нельзя удалить: на неё ссылается таблица " + other.getName());
                }
            }
        }
        tables.remove(key(tableName));
        storage.delete(table.getName());
    }

    @Override
    public void createIndex(String indexName, String tableName, String columnName) {
        checkIdentifier(indexName);
        Table table = table(tableName);
        table.addIndex(indexName, columnName);
        storage.save(table);
    }

    @Override
    public List<String> listTables() {
        List<String> names = new ArrayList<>();
        for (Table table : tables.values()) {
            names.add(table.getName());
        }
        return names;
    }

    @Override
    public List<Column> describeTable(String tableName) {
        return table(tableName).getColumns();
    }

    @Override
    public int insert(String tableName, List<String> columns, List<List<Object>> rows) {
        Table table = table(tableName);
        int width = table.getColumns().size();
        int[] positions = resolveInsertPositions(table, columns);

        List<Row> prepared = new ArrayList<>();
        Set<Object> keysInBatch = new HashSet<>();
        for (List<Object> values : rows) {
            if (values.size() != positions.length) {
                throw new DbException("Ожидалось значений: " + positions.length
                        + ", получено: " + values.size());
            }
            Object[] cells = new Object[width];
            for (int i = 0; i < positions.length; i++) {
                cells[positions[i]] = coerce(table.column(positions[i]), values.get(i));
            }
            Object pk = cells[table.getPkPosition()];
            if (pk == null) {
                throw new DbException("Не задано значение первичного ключа "
                        + table.column(table.getPkPosition()).getName());
            }
            if (table.containsPk(pk) || !keysInBatch.add(pk)) {
                throw new DbException("Нарушение уникальности первичного ключа: " + pk);
            }
            for (int i = 0; i < width; i++) {
                checkForeignKeyValue(table.column(i), cells[i]);
            }
            prepared.add(new Row(cells));
        }

        for (Row row : prepared) {
            table.insertRow(row);
        }
        storage.save(table);
        return prepared.size();
    }

    private int[] resolveInsertPositions(Table table, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            int[] all = new int[table.getColumns().size()];
            for (int i = 0; i < all.length; i++) {
                all[i] = i;
            }
            return all;
        }
        int[] result = new int[columns.size()];
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < result.length; i++) {
            result[i] = table.columnIndex(columns.get(i));
            if (!seen.add(result[i])) {
                throw new DbException("Столбец указан дважды: " + columns.get(i));
            }
        }
        return result;
    }

    @Override
    public QueryResult select(String tableName, List<String> columns, Condition where) {
        Table table = table(tableName);

        boolean all = columns == null || columns.isEmpty()
                || (columns.size() == 1 && "*".equals(columns.get(0)));
        List<Integer> positions = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (all) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                positions.add(i);
                names.add(table.column(i).getName());
            }
        } else {
            for (String column : columns) {
                int position = table.columnIndex(column);
                positions.add(position);
                names.add(table.column(position).getName());
            }
        }

        Match match = findRows(table, where);
        List<List<Object>> result = new ArrayList<>(match.rows.size());
        for (Row row : match.rows) {
            List<Object> line = new ArrayList<>(positions.size());
            for (int position : positions) {
                line.add(row.get(position));
            }
            result.add(line);
        }
        return new QueryResult(names, result, match.usedIndex);
    }

    @Override
    public int update(String tableName, Map<String, Object> assignments, Condition where) {
        Table table = table(tableName);
        if (assignments == null || assignments.isEmpty()) {
            throw new DbException("Не указано, что изменять (SET)");
        }

        Map<Integer, Object> prepared = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : assignments.entrySet()) {
            int position = table.columnIndex(entry.getKey());
            if (position == table.getPkPosition()) {
                throw new DbException("Первичный ключ изменять нельзя");
            }
            Column column = table.column(position);
            Object value = coerce(column, entry.getValue());
            checkForeignKeyValue(column, value);
            prepared.put(position, value);
        }

        List<Row> targets = findRows(table, where).rows;
        for (Row row : targets) {
            for (Map.Entry<Integer, Object> entry : prepared.entrySet()) {
                table.setValue(row, entry.getKey(), entry.getValue());
            }
        }
        if (!targets.isEmpty()) {
            storage.save(table);
        }
        return targets.size();
    }

    @Override
    public int delete(String tableName, Condition where) {
        Table table = table(tableName);
        List<Row> targets = findRows(table, where).rows;
        for (Row row : targets) {
            checkNotReferenced(table, row.get(table.getPkPosition()));
        }
        for (Row row : targets) {
            table.removeRow(row);
        }
        if (!targets.isEmpty()) {
            storage.save(table);
        }
        return targets.size();
    }

    private static class Match {
        final List<Row> rows;
        final boolean usedIndex;

        Match(List<Row> rows, boolean usedIndex) {
            this.rows = rows;
            this.usedIndex = usedIndex;
        }
    }

    private Match findRows(Table table, Condition where) {
        if (where == null) {
            return new Match(new ArrayList<>(table.allRows()), false);
        }
        int position = table.columnIndex(where.getColumn());
        Condition condition = where.withValue(coerce(table.column(position), where.getValue()));

        if (condition.getOperator() == Operator.EQ && condition.getValue() != null) {
            if (position == table.getPkPosition()) {
                List<Row> found = new ArrayList<>();
                Row row = table.findByPk(condition.getValue());
                if (row != null) {
                    found.add(row);
                }
                return new Match(found, true);
            }
            HashIndex index = table.findIndexByColumn(position);
            if (index != null) {
                List<Row> found = new ArrayList<>();
                for (Object pk : index.find(condition.getValue())) {
                    found.add(table.findByPk(pk));
                }
                return new Match(found, true);
            }
        }

        List<Row> found = new ArrayList<>();
        for (Row row : table.allRows()) {
            if (condition.matches(row.get(position))) {
                found.add(row);
            }
        }
        return new Match(found, false);
    }

    private static String key(String name) {
        return name.toLowerCase();
    }

    private Table table(String name) {
        Table table = tables.get(key(name));
        if (table == null) {
            throw new DbException("Таблица не найдена: " + name);
        }
        return table;
    }

    private static void checkIdentifier(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new DbException("Недопустимое имя: " + name);
        }
    }

    private static Object coerce(Column column, Object value) {
        try {
            return column.getType().coerce(value);
        } catch (DbException e) {
            throw new DbException("Столбец " + column.getName() + ": " + e.getMessage());
        }
    }

    private void checkForeignKeyDefinition(Column column) {
        Table parent = table(column.getRefTable());
        int position = parent.columnIndex(column.getRefColumn());
        if (position != parent.getPkPosition()) {
            throw new DbException("Внешний ключ должен ссылаться на первичный ключ таблицы "
                    + parent.getName());
        }
        if (parent.column(position).getType() != column.getType()) {
            throw new DbException("Тип столбца " + column.getName()
                    + " не совпадает с типом " + parent.getName() + "." + parent.column(position).getName());
        }
    }

    private void checkForeignKeyValue(Column column, Object value) {
        if (!column.hasForeignKey() || value == null) {
            return;
        }
        Table parent = table(column.getRefTable());
        if (!parent.containsPk(value)) {
            throw new DbException("Нарушение внешнего ключа: в таблице " + parent.getName()
                    + " нет записи со значением " + value);
        }
    }

    private void checkNotReferenced(Table parent, Object pk) {
        for (Table other : tables.values()) {
            for (int i = 0; i < other.getColumns().size(); i++) {
                Column column = other.column(i);
                if (column.hasForeignKey() && column.getRefTable().equalsIgnoreCase(parent.getName())) {
                    for (Row row : other.allRows()) {
                        if (pk.equals(row.get(i))) {
                            throw new DbException("Нельзя удалить запись " + pk + ": на неё ссылается таблица "
                                    + other.getName());
                        }
                    }
                }
            }
        }
    }
}