/*
 * Copyright 2009 Borislav Iordanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple JSON parser - copied and adapted from the StringTree library.
 */
public class JSONReader
{
    private static final Object OBJECT_END = new Object();
    private static final Object ARRAY_END = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();
    public static final int FIRST = 0;
    public static final int CURRENT = 1;
    public static final int NEXT = 2;

    private static Map<Character, Character> escapes = new HashMap<Character, Character>();
    static {
        escapes.put(new Character('"'), new Character('"'));
        escapes.put(new Character('\\'), new Character('\\'));
        escapes.put(new Character('/'), new Character('/'));
        escapes.put(new Character('b'), new Character('\b'));
        escapes.put(new Character('f'), new Character('\f'));
        escapes.put(new Character('n'), new Character('\n'));
        escapes.put(new Character('r'), new Character('\r'));
        escapes.put(new Character('t'), new Character('\t'));
    }

    private CharacterIterator it;
    private char c;
    private Object token;
    private StringBuffer buf = new StringBuffer();

    private char next() {
        c = it.next();
        return c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            next();
        }
    }

    public Object read(CharacterIterator ci, int start) {
        it = ci;
        switch (start) {
        case FIRST:
            c = it.first();
            break;
        case CURRENT:
            c = it.current();
            break;
        case NEXT:
            c = it.next();
            break;
        }
        return read();
    }

    public Object read(CharacterIterator it) {
        return read(it, NEXT);
    }

    public Object read(String string) {
        return read(new StringCharacterIterator(string), FIRST);
    }

    private Object read() {
        skipWhiteSpace();
        char ch = c;
        next();
        switch (ch) {
            case '"': token = string(); break;
            case '[': token = array(); break;
            case ']': token = ARRAY_END; break;
            case ',': token = COMMA; break;
            case '{': token = object(); break;
            case '}': token = OBJECT_END; break;
            case ':': token = COLON; break;
            case 't':
                if (c != 'r' || next() != 'u' || next() != 'e')
                	throw new RuntimeException("Invalid JSON token: expected 'true' keyword.");
                token = Boolean.TRUE;
                break;
            case'f':
                if (c != 'a' || next() != 'l' || next() != 's' || next() != 'e')
                	throw new RuntimeException("Invalid JSON token: expected 'false' keyword.");
                token = Boolean.FALSE;
                break;
            case 'n':
                if (c != 'u' || next() != 'l' || next() != 'l')
                	throw new RuntimeException("Invalid JSON token: expected 'null' keyword.");
                token = null;
                break;
            default:
                c = it.previous();
                if (Character.isDigit(c) || c == '-') {
                    token = number();
                }
        }
        // System.out.println("token: " + token); // enable this line to see the token stream
        return token;
    }

    private Object object() {
        Map<Object, Object> ret = new HashMap<Object, Object>();
        Object key = read();
        while (token != OBJECT_END) {
            read(); // should be a colon
            if (token != OBJECT_END) {
                ret.put(key, read());
                if (read() == COMMA) {
                    key = read();
                }
            }
        }

        return ret;
    }

    private Object array() {
        List<Object> ret = new ArrayList<Object>();
        Object value = read();
        while (token != ARRAY_END) {
            ret.add(value);
            if (read() == COMMA) {
                value = read();
            }
        }
        return ret;
    }

    private Object number() {
        int length = 0;
        boolean isFloatingPoint = false;
        buf.setLength(0);

        if (c == '-') {
            add();
        }
        length += addDigits();
        if (c == '.') {
            add();
            length += addDigits();
            isFloatingPoint = true;
        }
        if (c == 'e' || c == 'E') {
            add();
            if (c == '+' || c == '-') {
                add();
            }
            addDigits();
            isFloatingPoint = true;
        }

        String s = buf.toString();
        return isFloatingPoint
            ? (length < 17) ? (Object)Double.valueOf(s) : new BigDecimal(s)
            : (length < 20) ? (Object)Long.valueOf(s) : new BigInteger(s);
    }

    private int addDigits() {
        int ret;
        for (ret = 0; Character.isDigit(c); ++ret) {
            add();
        }
        return ret;
    }

    private Object string() {
        buf.setLength(0);
        while (c != '"') {
            if (c == '\\') {
                next();
                if (c == 'u') {
                    add(unicode());
                } else {
                    Object value = escapes.get(new Character(c));
                    if (value != null) {
                        add(((Character) value).charValue());
                    }
                }
            } else {
                add();
            }
        }
        next();

        return buf.toString();
    }

    private void add(char cc) {
        buf.append(cc);
        next();
    }

    private void add() {
        add(c);
    }

    private char unicode() {
        int value = 0;
        for (int i = 0; i < 4; ++i) {
            switch (next()) {
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                value = (value << 4) + c - '0';
                break;
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                value = (value << 4) + (c - 'a') + 10;
                break;
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                value = (value << 4) + (c - 'A') + 10;
                break;
            }
        }
        return (char) value;
    }
}
