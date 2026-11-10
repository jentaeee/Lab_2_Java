package com.minidb.query;

import com.minidb.exception.DbException;

public class Condition {
    private final String column;
    private final Operator operator;
    private final Object value;

    public Condition(String column, Operator operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public Condition withValue(Object newValue) {
        return new Condition(column, operator, newValue);
    }

    public boolean matches(Object cell) {
        if (cell == null || value == null) {
            return false;
        }
        int cmp = compare(cell, value);
        switch (operator) {
            case EQ: return cmp == 0;
            case NE: return cmp != 0;
            case GT: return cmp > 0;
            case LT: return cmp < 0;
            case GE: return cmp >= 0;
            case LE: return cmp <= 0;
            default: throw new IllegalStateException("Неизвестный оператор");
        }
    }

    @SuppressWarnings("unchecked")
    private static int compare(Object a, Object b) {
        if (a instanceof Number && b  instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number)b).doubleValue());
        }
        if (a instanceof Comparable && a.getClass() == b.getClass()) {
            return ((Comparable<Object>) a).compareTo(b);
        }
        throw new DbException("Нельзя сравнить значения" + a + " и " + b);
    }

}
