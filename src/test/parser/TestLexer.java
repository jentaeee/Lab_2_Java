package com.minidb.parser;

import java.util.List;

public class TestLexer {
    
    public static void main(String[] args) {
        String sql = "SELECT brand, price FROM cars WHERE price > 20000;";
        
        System.out.println("Входная строка: " + sql);
        System.out.println("Результат разбора");

        Lexer lexer = new Lexer(sql);
        List<Token> tokens = lexer.makeToken();

        for (Token t : tokens) {
            System.out.println(t);
        }
    }
}