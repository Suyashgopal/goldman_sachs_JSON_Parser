package com.typegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight recursive-descent JSON parser.
 * Converts JSON strings into Java Collections (Map, List, String, Number, Boolean, null).
 */
public class JsonParser {
    private final String json;
    private int pos;

    public JsonParser(String json) {
        this.json = json;
        this.pos = 0;
    }

    public Object parse() {
        skipWhitespace();
        return parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        
        throw new RuntimeException("Unexpected character at position " + pos + ": " + c);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        consume('{');
        skipWhitespace();
        
        if (peek() == '}') {
            consume('}');
            return map;
        }
        
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            consume(':');
            skipWhitespace();
            Object value = parseValue();
            map.put(key, value);
            
            skipWhitespace();
            if (peek() == '}') {
                consume('}');
                break;
            }
            consume(',');
        }
        
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        consume('[');
        skipWhitespace();
        
        if (peek() == ']') {
            consume(']');
            return list;
        }
        
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            
            if (peek() == ']') {
                consume(']');
                break;
            }
            consume(',');
        }
        
        return list;
    }

    private String parseString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        
        while (peek() != '"') {
            char c = next();
            if (c == '\\') {
                char escaped = next();
                switch (escaped) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        String hex = json.substring(pos, pos + 4);
                        pos += 4;
                        sb.append((char) Integer.parseInt(hex, 16));
                        break;
                    default:
                        throw new RuntimeException("Invalid escape sequence: \\" + escaped);
                }
            } else {
                sb.append(c);
            }
        }
        
        consume('"');
        return sb.toString();
    }

    private Number parseNumber() {
        int start = pos;
        
        if (peek() == '-') next();
        
        if (peek() == '0') {
            next();
        } else {
            while (Character.isDigit(peek())) next();
        }
        
        boolean isDouble = false;
        
        if (peek() == '.') {
            isDouble = true;
            next();
            while (Character.isDigit(peek())) next();
        }
        
        if (peek() == 'e' || peek() == 'E') {
            isDouble = true;
            next();
            if (peek() == '+' || peek() == '-') next();
            while (Character.isDigit(peek())) next();
        }
        
        String numStr = json.substring(start, pos);
        return isDouble ? Double.parseDouble(numStr) : Long.parseLong(numStr);
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true", pos)) {
            pos += 4;
            return true;
        } else if (json.startsWith("false", pos)) {
            pos += 5;
            return false;
        }
        throw new RuntimeException("Invalid boolean at position " + pos);
    }

    private Object parseNull() {
        if (json.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid null at position " + pos);
    }

    private char peek() {
        if (pos >= json.length()) return '\0';
        return json.charAt(pos);
    }

    private char next() {
        if (pos >= json.length()) throw new RuntimeException("Unexpected end of input");
        return json.charAt(pos++);
    }

    private void consume(char expected) {
        char c = next();
        if (c != expected) {
            throw new RuntimeException("Expected '" + expected + "' but got '" + c + "' at position " + (pos - 1));
        }
    }

    private void skipWhitespace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }
}
