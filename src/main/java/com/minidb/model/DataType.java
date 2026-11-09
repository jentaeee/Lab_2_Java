package com.minidb.model;

import com.minidb.exception.DbException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public enum DataType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;

    public static DataType fromString(String name) {
        switch(name.trim().toUpperCase()) {
            case "INT":
            case "INTEGER":
                return INT;
            case "DOUBLE":
            case "FLOAT":
            case "REAL":
                return DOUBLE;
            case "STRING":
            case "TEXT":
            case "VARCHAR":
                return STRING;
            case "DATE":
                return DATE;
            case "BOOLEAN":
                return BOOLEAN;
            default:
                throw new DbException("Неизвестный тип данных: " + name);
        }
    }

    public Object coerce(Object value){
        if (value == null) {
            return null;
        }
        try {
            switch (this) {
                case INT:
                    if (value instanceof Integer) {
                        return value;
                    }
                    if (value instanceof Long) {
                        long l = (Long) value;
                        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                            return (int) l;
                        }
                    }
                    break;
                case DOUBLE:
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                case STRING:
                    if (value instanceof String) {
                        return value;
                    }
                case DATE:
                    if (value instanceof LocalDate) {
                        return value;
                    }
                    if (value instanceof String) {
                        return LocalDate.parse(((String) value).trim());
                    }
                    break;
                case BOOLEAN:
                    if (value instanceof Boolean) {
                        return value;
                    }
                    break;
            }
        } catch (DateTimeParseException e) {
            //
        } throw new DbException("Значение '" + value + "' не подходит для типа" + this);
    }
}
