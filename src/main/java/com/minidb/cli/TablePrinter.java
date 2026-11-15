package com.minidb.cli;
import com.minidb.query.QueryResult;
import java.util.*;

public class TablePrinter {
    
    public static void print(QueryResult result) {
        List<String> columns = result.getColumns();
        List<List<Object>> rows = result.getRows();

        if (rows.isEmpty()) {
            System.out.println("Пустой результат.");
            return;
        }

        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).length();
            for (List<Object> row : rows) {
                String val = String.valueOf(row.get(i));
                if (val.length() > widths[i]) {
                    widths[i] = val.length();
                }
            }
        }

        printLine(widths);
        for (int i = 0; i < columns.size(); i++) {
            System.out.printf("| %-" + widths[i] + "s ", columns.get(i));
        }
        System.out.println("|");
        printLine(widths);

        for (List<Object> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                String val = String.valueOf(row.get(i));
                System.out.printf("| %-" + widths[i] + "s ", val);
            }
            System.out.println("|");
        }
        printLine(widths);
        
        System.out.println("Всего строк: " + rows.size() + 
            (result.isUsedIndex() ? " (использован индекс)" : " (полный перебор)"));
        System.out.println();
    }

    private static void printLine(int[] widths) {
        for (int width : widths) {
            System.out.print("+");
            for (int i = 0; i < width + 2; i++) {
                System.out.print("-");
            }
        }
        System.out.println("+");
    }
}
