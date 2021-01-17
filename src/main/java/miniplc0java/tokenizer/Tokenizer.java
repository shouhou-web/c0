package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;

import miniplc0java.util.Pos;

import java.nio.CharBuffer;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek))
            return lexUIntOrDouble();
        else if (Character.isAlphabetic(peek) || peek == '_')
            return lexIdentOrKeyword();
        else if (peek == '"')
            return lexString();
        else if (peek == '\'')
            return lexChar();
        else
            return lexOperatorOrUnknown();
    }

    private Token lexString() throws TokenizeError {
        // 跳过双引号"
        Pos prePos = it.currentPos();
        it.nextChar();
        StringBuilder ret = new StringBuilder("");
        int flag = 0; // 用于判别转义
        while (!it.isEOF()) {
            if (flag == 1) {
                System.out.println("flag == 1");
                if (isEscapeSequence(it.peekChar())) {
                    flag = 0;
                } else {
                    System.out.println("flag break");
                    break;
                }
            } else if (it.peekChar() == '\\') {
                flag = 1;
                System.out.println("flag = 1");
            } else if (it.peekChar() == '\"')
                break;
            ret.append(it.peekChar());
            it.nextChar();
        }
        // 退出时代表正常结束或者识别错误
        // 正常结束时下一位应为"
        // 否则是提前出现"或者\单独出现，错误
        if (it.peekChar() == '"') {
            it.nextChar();
            return new Token(TokenType.STRING_LITEREAL, ret, prePos, it.currentPos());
        }
        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
    }

    private Token lexChar() throws TokenizeError {
        Pos prePos = it.currentPos();
        it.nextChar();
        char ret;
        if (it.peekChar() == '\\') {
            it.nextChar();
            switch (it.peekChar()) {
                case '\\':
                    ret = '\\';
                    break;
                case 'r':
                    ret = '\r';
                    break;
                case 'n':
                    ret = '\n';
                    break;
                case 't':
                    ret = '\t';
                    break;
                case '"':
                    ret = '"';
                    break;
                case '\'':
                    ret = '\'';
                    break;
                default:
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
        } else if (it.peekChar() == '\'')
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        else
            ret = it.peekChar();
        it.nextChar();
        return new Token(TokenType.STRING_LITEREAL, ret, prePos, it.currentPos());
    }

    private boolean isEscapeSequence(char c) {
        return c == '\\' || c == 'r' || c == 'n' || c == 't' || c == '"' || c == '\'';
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        Pos prePos = it.currentPos();
        StringBuilder ret = new StringBuilder("");
        ret.append(lexDigit());
        if (it.peekChar() == '.') {
            ret.append(it.peekChar());
            it.nextChar();
            ret.append(lexDigit());
            if (it.peekChar() == 'e' || it.peekChar() == 'E') {
                ret.append(it.peekChar());
                it.nextChar();
                if (it.peekChar() == '+' || it.peekChar() == '-') {
                    ret.append(it.peekChar());
                    it.nextChar();
                }
                ret.append(lexDigit());
            }
            return new Token(TokenType.DOUBLE_LITERAL, Double.parseDouble(ret.toString()), prePos, it.currentPos());
        }
        return new Token(TokenType.Uint_LITERAL, Integer.parseInt(ret.toString()), prePos, it.currentPos());
    }

    private String lexDigit() throws TokenizeError {
        StringBuilder ret = new StringBuilder("");
        boolean flag = true; // 用于记录是否进入循环
        while (!it.isEOF() && Character.isDigit(it.peekChar())) {
            flag = false;
            ret.append(it.peekChar());
            it.nextChar();
        }
        if (flag)
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        return ret.toString();
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        Pos prePos = it.currentPos();
        StringBuilder ret = new StringBuilder("");
        TokenType type;
        while (!it.isEOF() && (Character.isAlphabetic(it.peekChar()) || Character.isDigit(it.peekChar()) || it.peekChar() == '_')) {
            ret.append(it.peekChar());
            it.nextChar();
        }

        switch (ret.toString()) {
            case "fn":
                type = TokenType.FN_KW;
                break;
            case "let":
                type = TokenType.LET_KW;
                break;
            case "const":
                type = TokenType.CONST_KW;
                break;
            case "as":
                type = TokenType.AS_KW;
                break;
            case "while":
                type = TokenType.WHILE_KW;
                break;
            case "if":
                type = TokenType.IF_KW;
                break;
            case "else":
                type = TokenType.ELSE_KW;
                break;
            case "return":
                type = TokenType.RETURN_KW;
                break;
            case "break":
                type = TokenType.BREAK_KW;
                break;
            case "continue":
                type = TokenType.CONTINUE_KW;
                break;
            case "int":
                type = TokenType.INT_KW;
                break;
            case "double":
                type = TokenType.DOUBLE_KW;
                break;
            case "void":
                type = TokenType.VOID_KW;
                break;
            default:
                type = TokenType.IDENT;
                break;
        }
        return new Token(type, ret.toString(), prePos, it.currentPos());
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.peekChar()) {
            case '+':
                it.nextChar();
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
            case '-':
                // 填入返回语句
                it.nextChar();
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
            case '*':
                // 填入返回语句
                it.nextChar();
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());
            case ':':
                it.nextChar();
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case '/':
                // 填入返回语句
                it.nextChar();
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            case '=':
                // 填入返回语句
                it.nextChar();
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
            case '!':
                it.nextChar();
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            case '<':
                it.nextChar();
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
            case '>':
                it.nextChar();
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
            case ',':
                it.nextChar();
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case '"':
                it.nextChar();
                return new Token(TokenType.COLON, ';', it.previousPos(), it.currentPos());
            case ';':
                it.nextChar();
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            case '(':
                it.nextChar();
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
            case ')':
                it.nextChar();
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                it.nextChar();
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                it.nextChar();
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
