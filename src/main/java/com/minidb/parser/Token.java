package com.minidb.parser;

public class Token {
    
    public enum ValueType { KEYWORD, IDENTIFIER, NUMBER, STRING, SYMBOL, EOF}
    private final ValueType type;
    private final String value;

    public Token(ValueType type, String value) {
        this.type = type;
        this.value = value;
    }

    public ValueType getType() { return  type;}
    public String getValue() { return  value;}

    @Override 
    public String toString(){
        return type + "('" + value + "')";
    }
}
