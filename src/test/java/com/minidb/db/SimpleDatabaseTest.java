package com.minidb.db;

import com.minidb.exception.DbException;
import com.minidb.model.Column;
import com.minidb.model.DataType;
import com.minidb.query.Condition;
import com.minidb.query.Operator;
import com.minidb.query.QueryResult;
import com.minidb.storage.FileTableStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDatabaseTest {

    @TempDir
    Path dir;

    private SimpleDatabase db;

    @BeforeEach
    void setUp() {
        db = open();
        db.createTable("brands", Arrays.asList(
                new Column("id", DataType.INT, true),
                new Column("name", DataType.STRING, false)));
        db.createTable("cars", Arrays.asList(
                new Column("id", DataType.INT, true),
                new Column("brand_id", DataType.INT, false, "brands", "id"),
                new Column("model", DataType.STRING, false),
                new Column("price", DataType.DOUBLE, false),
                new Column("available", DataType.BOOLEAN, false)));
        db.insert("brands", null, rows(row(1, "Toyota"), row(2, "BMW")));
        db.insert("cars", null, rows(
                row(1, 1, "Camry", 25000.0, true),
                row(2, 2, "X5", 60000.0, false),
                row(3, 1, "Corolla", 18000.0, true)));
    }

    private SimpleDatabase open() {
        return new SimpleDatabase(new FileTableStorage(dir));
    }

    private static List<Object> row(Object... values) {
        return Arrays.asList(values);
    }

    @SafeVarargs
    private static List<List<Object>> rows(List<Object>... rows) {
        return Arrays.asList(rows);
    }

    @Test
    void selectAllReturnsEveryRow() {
        QueryResult result = db.select("cars", null, null);
        assertEquals(3, result.size());
        assertEquals(Arrays.asList("id", "brand_id", "model", "price", "available"), result.getColumns());
    }

    @Test
    void duplicatePrimaryKeyIsRejected() {
        assertThrows(DbException.class,
                () -> db.insert("cars", null, rows(row(1, 1, "Dup", 1.0, true))));
        assertEquals(3, db.select("cars", null, null).size());
    }

    @Test
    void batchInsertIsAtomic() {
        assertThrows(DbException.class,
                () -> db.insert("brands", null, rows(row(10, "Audi"), row(10, "Skoda"))));
        assertEquals(2, db.select("brands", null, null).size());
    }

    @Test
    void insertWithColumnListLeavesOthersNull() {
        db.insert("cars", Arrays.asList("id", "model"), rows(row(4, "Yaris")));
        QueryResult result = db.select("cars", Arrays.asList("model", "price"),
                new Condition("id", Operator.EQ, 4));
        assertEquals(1, result.size());
        assertEquals("Yaris", result.getRows().get(0).get(0));
        assertNull(result.getRows().get(0).get(1));
    }

    @Test
    void foreignKeyIsChecked() {
        assertThrows(DbException.class,
                () -> db.insert("cars", null, rows(row(9, 99, "Ghost", 1.0, true))));
    }

    @Test
    void wrongTypeIsRejected() {
        assertThrows(DbException.class,
                () -> db.insert("brands", null, rows(row("abc", "Fiat"))));
    }

    @Test
    void selectWithCondition() {
        QueryResult result = db.select("cars", Arrays.asList("model", "price"),
                new Condition("price", Operator.GT, 20000));
        assertEquals(2, result.size());
    }

    @Test
    void updateByCondition() {
        int changed = db.update("cars", Collections.<String, Object>singletonMap("price", 30000),
                new Condition("id", Operator.EQ, 2));
        assertEquals(1, changed);
        QueryResult result = db.select("cars", Arrays.asList("price"),
                new Condition("id", Operator.EQ, 2));
        assertEquals(Double.valueOf(30000.0), result.getRows().get(0).get(0));
    }

    @Test
    void deleteByCondition() {
        assertEquals(1, db.delete("cars", new Condition("model", Operator.EQ, "X5")));
        assertEquals(2, db.select("cars", null, null).size());
    }

    @Test
    void referencedRecordCannotBeDeleted() {
        assertThrows(DbException.class,
                () -> db.delete("brands", new Condition("id", Operator.EQ, 1)));
        assertThrows(DbException.class, () -> db.dropTable("brands"));
    }

    @Test
    void dataSurvivesRestart() {
        SimpleDatabase reopened = open();
        assertEquals(3, reopened.select("cars", null, null).size());
        assertEquals(2, reopened.listTables().size());
    }

    @Test
    void indexIsUsedAndSurvivesRestart() {
        Condition byModel = new Condition("model", Operator.EQ, "Camry");
        assertFalse(db.select("cars", null, byModel).isUsedIndex());

        db.createIndex("idx_cars_model", "cars", "model");
        QueryResult withIndex = db.select("cars", null, byModel);
        assertTrue(withIndex.isUsedIndex());
        assertEquals(1, withIndex.size());

        QueryResult afterRestart = open().select("cars", null, byModel);
        assertTrue(afterRestart.isUsedIndex());
        assertEquals(1, afterRestart.size());
    }

    @Test
    void indexStaysConsistentAfterUpdateAndDelete() {
        db.createIndex("idx_cars_model", "cars", "model");
        db.update("cars", Collections.<String, Object>singletonMap("model", "Camry Hybrid"),
                new Condition("id", Operator.EQ, 1));
        assertEquals(0, db.select("cars", null, new Condition("model", Operator.EQ, "Camry")).size());
        assertEquals(1, db.select("cars", null, new Condition("model", Operator.EQ, "Camry Hybrid")).size());

        db.delete("cars", new Condition("id", Operator.EQ, 1));
        assertEquals(0, db.select("cars", null, new Condition("model", Operator.EQ, "Camry Hybrid")).size());
    }
}
