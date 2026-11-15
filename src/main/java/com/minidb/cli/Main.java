package com.minidb.cli;
import com.minidb.db.*;
import com.minidb.engine.QueryExecutor;
import com.minidb.parser.Parser;
import com.minidb.query.QueryResult;
import com.minidb.storage.*;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        TableStorage storage = new FileTableStorage(Paths.get("data"));
        Database database = new SimpleDatabase(storage);
        Parser parser = new Parser();
        QueryExecutor executor = new QueryExecutor(database);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите SQL-запрос:");

        while (true) {
            System.out.print("sql> ");
            String line = scanner.nextLine().trim();
            
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }

            try {
                Object query = parser.parseStr(line);
                Object result = executor.processQuery(query);

                if (result instanceof QueryResult) {
                    TablePrinter.print((QueryResult) result);
                } else {
                    System.out.println(result);
                }
            } catch (Exception e) {
                System.err.println("Ошибка:" + e.getMessage());
            }
        }
        scanner.close();
    }
}