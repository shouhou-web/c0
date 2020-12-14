package miniplc0java.tokenizer;

public enum TokenType {
    /**
     * 空
     */
    None,
    /**---- 字面量 ------ */
    /**
     * 无符号整数
     */
    Uint_LITERAL,
    /**
     * 字符串常量
     */
    STRING_LITEREAL,
    /**
     * 浮点数常量
     */
    DOUBLE_LITERAL,
    /**
     * 字符型常量
     */
    CHAR_LITEREAL,

    /**---- 关键字 ------ */
    /**
     * fn
     */
    FN_KW,
    /**
     * let
     */
    LET_KW,
    /**
     * const
     */
    CONST_KW,
    /**
     * as
     */
    AS_KW,
    /**
     * while
     */
    WHILE_KW,
    /**
     * if
     */
    IF_KW,
    /**
     * else
     */
    ELSE_KW,
    /**
     * return
     */
    RETURN_KW,
    /**
     * break
     */
    BREAK_KW,
    /**
     * continue
     */
    CONTINUE_KW,

    /**---- 标识符 ------ */
    /**
     * 标识符
     */
    IDENT,

    /**---- 运算符 ------ */

    /**
     * 加号
     */
    PLUS,
    /**
     * 减号
     */
    MINUS,
    /**
     * 乘号
     */
    MUL,
    /**
     * 除号
     */
    DIV,
    /**
     * 等号
     */
    ASSIGN,
    /**
     * ==
     */
    EQ,
    /**
     * !=
     */
    NEQ,
    /**
     * <
     */
    LT,
    /**
     * >
     */
    GT,
    /**
     * LE
     */
    LE,
    /**
     * >=
     */
    GE,
    /**
     * 左括号
     */
    L_PAREN,
    /**
     * 右括号
     */
    R_PAREN,
    /**
     * 左大括号
     */
    L_BRACE,
    /**
     * 右大括号
     */
    R_BRACE,
    /**
     * ->
     */
    ARROW,
    /**
     * 逗号
     */
    COMMA,
    /**
     * 冒号
     */
    COLON,
    /**
     * 分号
     */
    SEMICOLON,
    /*** 文件结尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case PLUS:
                return "PlusSign";
            case MINUS:
                return "MinusSign";
            case MUL:
                return "MultiplicationSign";
            case DIV:
                return "DivSign";
            case ASSIGN:
                return "AsignSign";
            case EQ:
                return "EQSign";
            case GE:
                return "GESign";
            case GT:
                return "GTSign";
            case LE:
                return "LESign";
            case LT:
                return "LTSign";
            case NEQ:
                return "NEQSign";
            case ARROW:
                return "ArrowSign";
            case COLON:
                return "ColonSign";
            case COMMA:
                return "CommaSign";
            case SEMICOLON:
                return "Semicolon";
            case IDENT:
                return "Identifier";
            case L_PAREN:
                return "LeftParen";
            case R_PAREN:
                return "RightParen";
            case L_BRACE:
                return "LeftBrace";
            case R_BRACE:
                return "RightBrace";
            case Uint_LITERAL:
                return "UnsignedInteger";
            case CHAR_LITEREAL:
                return "Char";
            case DOUBLE_LITERAL:
                return "Double";
            case STRING_LITEREAL:
                return "String";
            case EOF:
                return "EndOfFile";
            default:
                return "InvalidToken";
        }
    }
}
