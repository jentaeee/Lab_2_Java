package com.minidb.query;

import com.minidb.exception.DbException;

public enum Operator {
    EQ("="), NE("!="), GT(">"), LT("<"), GE(">="), LE("<=");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Operator fromSymbol(String s) {
        if ("<>".equals(s)) {
            return NE;
        }
        for (Operator op: values()) {
            if (op.symbol.equals(s)) {
                return op;
            }
        }
        throw new DbException("Неизвестный оператор: " + s);
    }
}
