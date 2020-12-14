package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;

import miniplc0java.util.Pos;

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
        if (Character.isDigit(peek)) {
            return lexUInt();
        } else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUInt() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        Pos prePos = it.currentPos();
        int ret = 0;
        while (!it.isEOF() && Character.isDigit(it.peekChar())) {
            ret = ret * 10 + (it.peekChar() - '0');
            it.nextChar();
        }
        return new Token(TokenType.Uint_LITERAL, ret, prePos, it.currentPos());
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
        while (!it.isEOF() && (Character.isAlphabetic(it.peekChar()) || Character.isDigit(it.peekChar()))) {
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
