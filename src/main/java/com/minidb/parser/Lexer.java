package com.minidb.parser;
import java.util.*;

public class Lexer {
    
    private final String input;
    private int position = 0;
    private static final List<String> KeywordsList = Arrays.asList("INSERT", "INTO", "VALUES", "SELECT", "FROM", "WHERE", "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DROP", "INDEX", "ON", "TRUE", "FALSE");

    public Lexer(String input){
        this.input = input;
    }

    public List<Token> makeToken() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            char c = input.charAt(position);
            if (Character.isWhitespace(c)) {
                position++;
                continue;
            }
            if (c == '\'') {
                tokens.add(readString());
                continue;
            }
            if (Character.isDigit(c) || c == '-') {
                tokens.add(readNumber());
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }
            if ("(),;=<>*".indexOf(c) >= 0) {
                tokens.add(new Token(Token.ValueType.SYMBOL, String.valueOf(c)));
                position++;
                continue;
            }
            throw new IllegalArgumentException("Неизвестный символ");
        }   
        tokens.add(new Token(Token.ValueType.EOF, ""));
        return tokens;   
}

    private Token readString() {
        position++;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && input.charAt(position) != '\'') {
            sb.append(input.charAt(position));
            position++;
        }
        position++; 
        return new Token(Token.ValueType.STRING, sb.toString());
    }

    private Token readNumber() {
        int start_index = position;
        if (input.charAt(position) == '-') position++;
        while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
            position++;
        }
        return new Token(Token.ValueType.NUMBER, input.substring(start_index, position));
    }

    private Token readIdentifierOrKeyword() {
        int start_index = position;
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            position++;
        }
        String word = input.substring(start_index, position);
        if (KeywordsList.contains(word.toUpperCase())) {
            return new Token(Token.ValueType.KEYWORD, word.toUpperCase());
        }
        return new Token(Token.ValueType.IDENTIFIER, word);
    }
}