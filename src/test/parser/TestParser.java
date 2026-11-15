package com.minidb.parser;

public class TestParser {
    public static void main(String[] args) {
        Parser parser = new Parser();
        
        System.out.println("Тест 1: INSERT");
        parser.parseStr("INSERT INTO cars VALUES (1, 'Toyota', 25000.0, TRUE);");
        System.out.println("Успешно!");

        System.out.println("\nТест 2: SELECT");
        parser.parseStr("SELECT brand, price FROM cars WHERE price > 20000;");
        System.out.println("Успешно!");

        System.out.println("\nТест 3: UPDATE");
        parser.parseStr("UPDATE cars SET price = 30000 WHERE id = 2;");
        System.out.println("Успешно!");

        System.out.println("\nТест 4: DELETE");
        parser.parseStr("DELETE FROM cars WHERE brand = 'Toyota';");
        System.out.println("Успешно!");
    }
}
