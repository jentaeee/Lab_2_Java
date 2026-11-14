package com.minidb.bench;

import com.minidb.db.Database;
import com.minidb.db.SimpleDatabase;
import com.minidb.model.Column;
import com.minidb.model.DataType;
import com.minidb.query.Condition;
import com.minidb.query.Operator;
import com.minidb.storage.FileTableStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PerformanceBenchmark {
    private static final int QUERIES = 200;

    public static void main(String[] args) throws Exception {
        System.out.println("Строк | без индекса, мс | с индексом, мс | ускорение");
        for (int size : new int[]{1_000, 10_000, 100_000}) {
            run(size);
        }
    }

    private static void run(int size) throws Exception {
        Path dir = Files.createTempDirectory("minidb-bench");
        Database db = new SimpleDatabase(new FileTableStorage(dir));
        db.createTable("items", Arrays.asList(
                new Column("id", DataType.INT, true),
                new Column("name", DataType.STRING, false),
                new Column("price", DataType.DOUBLE, false)));

        List<List<Object>> rows = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            rows.add(Arrays.<Object>asList(i, "item_" + i, i * 1.5));
        }
        db.insert("items", null, rows);

        Condition target = new Condition("name", Operator.EQ, "item_" + (size - 5));

        measure(db, target, 20);                       // прогрев JVM
        double withoutIndex = measure(db, target, QUERIES);

        db.createIndex("idx_items_name", "items", "name");
        measure(db, target, 20);
        double withIndex = measure(db, target, QUERIES);

        System.out.printf("%d | %.4f | %.4f | x%.0f%n", size, withoutIndex, withIndex, withoutIndex / withIndex);
    }

    private static double measure(Database db, Condition condition, int times) {
        long start = System.nanoTime();
        for (int i = 0; i < times; i++) {
            db.select("items", null, condition);
        }
        return (System.nanoTime() - start) / 1_000_000.0 / times;
    }
}