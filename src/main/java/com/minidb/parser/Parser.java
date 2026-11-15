package com.minidb.parser;
import com.minidb.query.*;
import com.minidb.parser.ast.*;
import java.util.*;
import com.minidb.model.*;

public class Parser {

    private List<Token> tokens_list;
    private int position;

    private Token currentToken() {
        return tokens_list.get(position);
    }

    private Token nextToken() {
        return tokens_list.get(position++);
    }

    private boolean matchPosition(String exp) {
        if (currentToken().getValue().equals(exp)) {
            position++;
            return true;
        }
        return false;
    }

    private void expectedToken(String exp) {
        Token token = nextToken();
        if (!token.getValue().equals(exp)) {
            throw new IllegalArgumentException("Неверный аргумент, ожидалось '" + exp + "'");
        }
    }

    private String expectedIdentifier() {
        Token token = nextToken();
        if (token.getType() != Token.ValueType.IDENTIFIER && token.getType() != Token.ValueType.KEYWORD) {
            throw new IllegalArgumentException("Неверный аргумент, ожидался идентификатор");
        }
        return token.getValue();
    }

    private InsertQuery parseInsert() {
        expectedToken("INSERT");
        expectedToken("INTO");
        String tableName = expectedIdentifier();

        List<String> cols = null;
        if (matchPosition("(")) {
            cols = new ArrayList<>();
            do {
                cols.add(expectedIdentifier());
            } while (matchPosition(","));
            expectedToken(")");
        }

        expectedToken("VALUES");
        List<List<Object>> rows = new ArrayList<>();
        
        do {
            expectedToken("(");
            List<Object> row = new ArrayList<>();
            do {
                row.add(parseValue());
            } while (matchPosition(","));
            expectedToken(")");
            rows.add(row);
        } while (matchPosition(","));

        matchPosition(";");
        return new InsertQuery(tableName, cols, rows);
    }

    private SelectQuery parseSelect() {
        expectedToken("SELECT");
        List<String> cols = new ArrayList<>();
        
        if (matchPosition("*")) {
            cols.add("*");
        } else {
            do {
                cols.add(expectedIdentifier());
            } while (matchPosition(","));
        }

        expectedToken("FROM");
        String tableName = expectedIdentifier();
        
        Condition where = null;
        if (matchPosition("WHERE")) {
            where = parseCondition();
        }
        
        matchPosition(";");
        return new SelectQuery(tableName, cols, where);
    }

    private UpdateQuery parseUpdate() {
        expectedToken("UPDATE");
        String tableName = expectedIdentifier();
        expectedToken("SET");

        Map<String, Object> assign = new LinkedHashMap<>();
        do {
            String col = expectedIdentifier();
            expectedToken("=");
            assign.put(col, parseValue());
        } while (matchPosition(","));

        Condition where = null;
        if (matchPosition("WHERE")) {
            where = parseCondition();
        }

        matchPosition(";");
        return new UpdateQuery(tableName, assign, where);
    }

    private DeleteQuery parseDelete() {
        expectedToken("DELETE");
        expectedToken("FROM");
        String tableName = expectedIdentifier();

        Condition where = null;
        if (matchPosition("WHERE")) {
            where = parseCondition();
        }

        matchPosition(";");
        return new DeleteQuery(tableName, where);
    }

    private Condition parseCondition() {
        String column = expectedIdentifier();
        String opStr = nextToken().getValue();
        
        Operator operator;
        switch (opStr) {
            case "=": operator = Operator.EQ; break;
            case ">": operator = Operator.GT; break;
            case "<": operator = Operator.LT; break;
            default: throw new IllegalArgumentException("Неизвестный оператор: " + opStr);
        }

        Object value = parseValue();
        return new Condition(column, operator, value);
    }

    private Object parseValue() {
        Token token = nextToken();
        
        if (token.getType() == Token.ValueType.NUMBER) {
            String value = token.getValue();
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } else if (token.getType() == Token.ValueType.STRING) {
            return token.getValue();
        } else if (token.getType() == Token.ValueType.KEYWORD) {
            if (token.getValue().equals("TRUE")) return Boolean.TRUE;
            if (token.getValue().equals("FALSE")) return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Ожидалось значение");
    }

    private Object parseCreate() {
        expectedToken("CREATE");
        
        if (matchPosition("TABLE")) {
            String tableName = expectedIdentifier();
            expectedToken("(");
            List<Column> cols = new ArrayList<>();
            
            do {
                cols.add(parseColumn());
            } while (matchPosition(","));
            
            expectedToken(")");
            matchPosition(";");
            return new CreateTableQuery(tableName, cols);
        }
        
        if (matchPosition("INDEX")) {
            String indexName = expectedIdentifier();
            expectedToken("ON");
            String tableName = expectedIdentifier();
            expectedToken("(");
            String colName = expectedIdentifier();
            expectedToken(")");
            matchPosition(";");
            return new CreateIndexQuery(indexName, tableName, colName);
        }
        
        throw new IllegalArgumentException("Ожидалось TABLE или INDEX после CREATE");
    }

    private Object parseDrop() {
        expectedToken("DROP");
        expectedToken("TABLE");
        String tableName = expectedIdentifier();
        matchPosition(";");
        return new DropTableQuery(tableName);
    }

    private Column parseColumn() {
        String colName = expectedIdentifier();
        String typeStr = expectedIdentifier().toUpperCase();
        
        DataType type;
        switch (typeStr) {
            case "INT": type = DataType.INT; break;
            case "DOUBLE": type = DataType.DOUBLE; break;
            case "STRING": type = DataType.STRING; break;
            case "BOOLEAN": type = DataType.BOOLEAN; break;
            case "DATE": type = DataType.DATE; break;
            default: throw new IllegalArgumentException("Неизвестный тип: " + typeStr);
        }
        
        boolean isPrimary = false;
        String refTable = null;
        String refCol = null;
        
        if (matchPosition("PRIMARY")) {
            expectedToken("KEY");
            isPrimary = true;
        }
        
        if (matchPosition("REFERENCES")) {
            refTable = expectedIdentifier();
            expectedToken("(");
            refCol = expectedIdentifier();
            expectedToken(")");
        }
        
        if (refTable != null) {
            return new Column(colName, type, isPrimary, refTable, refCol);
        }
        return new Column(colName, type, isPrimary);
    }


    public Object parseStr(String sql_input) {
        Lexer lexer = new Lexer(sql_input);
        tokens_list = lexer.makeToken();
        position = 0;
        Token firstToken = currentToken();
        if (firstToken.getType() != Token.ValueType.KEYWORD) {
            throw new IllegalArgumentException("Ключевое слово должно идти первым");
        }

        switch (firstToken.getValue()) {
            case "INSERT": return parseInsert();
            case "SELECT": return parseSelect();
            case "UPDATE": return parseUpdate();
            case "DELETE": return parseDelete();
            case "CREATE": return parseCreate();
            case "DROP": return parseDrop();
            default:
                throw new IllegalArgumentException("Неизвестный запрос: " + firstToken.getValue());
        }
    }
}
