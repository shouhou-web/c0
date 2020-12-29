package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Analyser {
    Tokenizer tokenizer;

    /**
     * 待修改
     */
    ArrayList<Instruction> instructions;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * 符号表
     */
    SymbolTable currentTable = new SymbolTable();

    /**
     * 下一个变量的栈偏移
     */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param isFunction    是否已赋值
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, boolean isFunction, Pos curPos) throws AnalyzeError {
//        if (this.symbolTable.get(name) != null) {
//            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
//        } else {
//            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, isFunction, getNextVariableOffset()));
//        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
//        var entry = this.symbolTable.get(name);
//        if (entry == null) {
//            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
//        } else {
//            entry.setInitialized(true);
//        }
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 检查函数返回值TY的
     *
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expectTY() throws CompileError {
        var token = peek();
        if (check(TokenType.INT_KW) || check(TokenType.VOID_KW) || check(TokenType.DOUBLE_KW)) {
            return next();
        } else {
            throw new ExpectedTokenError(TokenType.TY_KW, token);
        }
    }

    /**
     * 检查变量定义TY的，暂时没想好
     *
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expectParam_TY() throws CompileError {
        var token = peek();
        if (check(TokenType.INT_KW) || check(TokenType.DOUBLE_KW)) {
            return next();
        } else {
            throw new ExpectedTokenError(TokenType.TY_KW, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    private void analyseProgram() throws CompileError {
        // program -> item*
        while (check(TokenType.LET_KW)
                || check(TokenType.CONST_KW)
                || check(TokenType.FN_KW))
            analyseItem();
    }

    private void analyseItem() throws CompileError {
        // item -> function | decl_stmt
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW))
            analyseDecl_Stmt();
        else if (check(TokenType.FN_KW))
            analyseFunction();
    }

    private void analyseFunction() throws CompileError {
        // function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
        expect(TokenType.FN_KW);

        // todo:加入符号表（不确定，待修改）
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();
        addSymbol(name, false, false, true, nameToken.getStartPos());

        // (
        expect(TokenType.L_PAREN);

        // function_param_list
        analyseFunctionParamList();

        // )->
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        // 类型
        var typeToken = expectTY();
        String type = typeToken.getValueString();

        // block_stmt
        analyseBlock_Stmt();

    }

    private void analyseFunctionParamList() throws CompileError {
        // function_param_list -> function_param (',' function_param)*

        while (check(TokenType.IDENT) || check(TokenType.CONST_KW))
            analyseFunctionParam();
    }

    private void analyseFunctionParam() throws CompileError {
        // function_param -> 'const'? IDENT ':' ty

        if (nextIf(TokenType.CONST_KW) != null) {
            // 常量
        }
        // 分析变量名
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();

        // :冒号
        expect(TokenType.COLON);

        // 类型
        var typeToken = expectParam_TY();
        String type = typeToken.getValueString();

        // todo:似乎还应该做些什么

    }

    private void analyseStmt() throws CompileError {
        // # 语句
        // stmt -> expr_stmt
        //       | decl_stmt
        //       | if_stmt
        //       | while_stmt
        //       | break_stmt
        //       | continue_stmt
        //       | return_stmt
        //       | block_stmt
        //       | empty_stmt

        switch (peekedToken.getTokenType()) {
            case L_BRACE:
                // block_stmt
                analyseBlock_Stmt();
                break;
            case SEMICOLON:
                // empty_stmt
                analyseEmpty_Stmt();
                break;
            case RETURN_KW:
                // return_stmt
                analyseReturn_Stmt();
                break;
            case CONTINUE_KW:
                // continue_stmt
                analyseContinue_Stmt();
                break;
            case BREAK_KW:
                // break_stmt
                analyseBreak_Stmt();
                break;
            case WHILE_KW:
                // while_stmt
                analyseWhile_Stmt();
                break;
            case IF_KW:
                // if_stmt
                analyseIf_Stmt();
                break;
            case LET_KW:
            case CONST_KW:
                // decl_stmt
                analyseDecl_Stmt();
                break;
            default:
                // expr_stmt
                analyseExpr_Stmt();
        }
    }

    private void analyseBlock_Stmt() throws CompileError {
        // block_stmt -> '{' stmt* '}'
        expect(TokenType.L_BRACE);

        // 分析语句
        while (!check(TokenType.R_BRACE))
            analyseStmt();

        expect(TokenType.R_BRACE);
    }


    private void analyseExpr_Stmt() throws CompileError {
        // expr_stmt -> expr ';'
        analyseExpr();
    }

    private void analyseDecl_Stmt() throws CompileError {
        // decl_stmt -> let_decl_stmt | const_decl_stmt
        // { let, const }
        if (check(TokenType.LET_KW))
            analyseLet_Decl_Stmt();
        else if (check(TokenType.CONST_KW))
            analyseConst_Decl_Stmt();
    }

    private void analyseLet_Decl_Stmt() throws CompileError {
        // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'

        expect(TokenType.LET_KW);

        // 分析变量名
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();

        // :
        expect(TokenType.COLON);

        // 类型
        var typeToken = expectParam_TY();
        String type = typeToken.getValueString();

        // ('=' expr)?
        if (nextIf(TokenType.ASSIGN) != null)
            analyseExpr();

        // todo:很明显，这里差东西

        // ;
        expect(TokenType.SEMICOLON);
    }

    private void analyseConst_Decl_Stmt() throws CompileError {
        // const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'

        expect(TokenType.CONST_KW);

        // 分析变量名
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();

        // :
        expect(TokenType.COLON);

        // 类型
        var typeToken = expectParam_TY();
        String type = typeToken.getValueString();

        // '=' expr
        expect(TokenType.ASSIGN);
        analyseExpr();

        // todo:很明显，这里差东西

        // ;
        expect(TokenType.SEMICOLON);
    }

    private void analyseIf_Stmt() throws CompileError {
        // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
        // { if }
        expect(TokenType.IF_KW);

        // todo:这里的表达式要干啥呢？？
        analyseExpr();

        // todo:是否应该在进入前保存一些状态
        analyseBlock_Stmt();

        // ('else' 'if' expr block_stmt)*
        while (nextIf(TokenType.ELSE_KW) != null) {
            expect(TokenType.IF_KW);

            // todo:这里的表达式要干啥呢？？
            analyseExpr();

            // todo:是否应该在进入前保存一些状态
            analyseBlock_Stmt();
        }

        // ('else' block_stmt)?
        if (nextIf(TokenType.ELSE_KW) != null) {
            // todo:是否应该在进入前保存一些状态
            analyseBlock_Stmt();
        }
    }

    private void analyseWhile_Stmt() throws CompileError {
        // while_stmt -> 'while' expr block_stmt
        //  { while }
        expect(TokenType.WHILE_KW);

        // todo:这里的表达式要干啥呢？？
        analyseExpr();

        // todo:是否应该在进入前保存一些状态
        analyseBlock_Stmt();
    }

    private void analyseBreak_Stmt() throws CompileError {
        // break_stmt -> 'break' ';'
        // { break }
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
    }

    private void analyseContinue_Stmt() throws CompileError {
        // continue_stmt -> 'continue' ';'
        // { continue }
        expect(TokenType.CONTINUE_KW);
        expect(TokenType.SEMICOLON);
    }

    private void analyseReturn_Stmt() throws CompileError {
        // return_stmt -> 'return' expr? ';'
        // { return }
        expect(TokenType.RETURN_KW);
        expect(TokenType.SEMICOLON);
    }

    private void analyseEmpty_Stmt() throws CompileError {
        // empty_stmt -> ';'
        // { ; }
        expect(TokenType.SEMICOLON);
    }

    private void analyseExpr() throws CompileError {
        // # 表达式
        // expr -> exprA
        analyseExprA();
    }

    private void analyseExprA() throws CompileError {
        // A -> B ( '=' A )*
        analyseExprB();
        // 避免连等
        if (nextIf(TokenType.ASSIGN) != null)
            analyseExprA();
    }

    private void analyseExprB() throws CompileError {
        // B -> C ( ( '<'|'>'|'<='|'>='|'=='|'!=' ) C )*
        analyseExprC();
        while (check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) || check(TokenType.GE) || check(TokenType.EQ) || check(TokenType.NEQ)) {
            var token = next();
            switch (token.getTokenType()) {
                case LT:
                case GT:
                case LE:
                case GE:
                case EQ:
                case NEQ:
                    // todo:这里应该做些啥
                    analyseExprC();
                default:
                    throw new ExpectedTokenError(TokenType.LT, peekedToken);
            }
        }
    }

    private void analyseExprC() throws CompileError {
        // C -> D ( {+|-} D )*
        analyseExprD();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            var token = next();
            switch (token.getTokenType()) {
                case PLUS:
                case MINUS:
                    // todo:这里应该做些啥
                    analyseExprD();
                default:
                    throw new ExpectedTokenError(TokenType.PLUS, token);
            }
        }
    }

    private void analyseExprD() throws CompileError {
        // D -> E ( {*|/} E )*
        analyseExprE();
        while (check(TokenType.MUL) || check(TokenType.DIV)) {
            var token = next();
            switch (token.getTokenType()) {
                case MUL:
                case DIV:
                    // todo:这里应该做些啥
                    analyseExprE();
                default:
                    throw new ExpectedTokenError(TokenType.MUL, token);
            }
        }

    }

    private void analyseExprE() throws CompileError {
        // E -> F [ 'as' type ]
        analyseExprF();
        if (nextIf(TokenType.AS_KW) != null) {
            var typeToken = expectParam_TY();
            String type = typeToken.getValueString();
        }
    }

    private void analyseExprF() throws CompileError {
        // F -> [ '-' ] G
        if (nextIf(TokenType.MINUS) != null) {
            // todo:F表达式的负号分析
        }
        analyseExprG();
    }

    private void analyseExprG() throws CompileError {
        // G -> '(' A ')' | H
        if (nextIf(TokenType.L_PAREN) != null) {
            analyseExprA();
            expect(TokenType.R_PAREN);
        }
        analyseExprH();
    }

    private void analyseExprH() throws CompileError {
        // H -> I | Ident [ '(' A (',' A)* ')' ]
        if (check(TokenType.IDENT)) {
            var nameToken = expect(TokenType.IDENT);
            String name = nameToken.getValueString();

            // 这里是函数调用
            if (nextIf(TokenType.L_PAREN) != null) {
                analyseExprA();
                while (nextIf(TokenType.COMMA) != null) {
                    analyseExprA();
                }
                expect(TokenType.R_PAREN);
                return;
            }

            // todo:这里是普通ident，应该检查符号表?
        }
        analyseExprI();
    }

    private void analyseExprI() throws CompileError {
        // I -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
        if (check(TokenType.Uint_LITERAL) || check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITEREAL) || check(TokenType.CHAR_LITEREAL))
            return;
        throw new ExpectedTokenError(TokenType.MUL, peekedToken);
    }
}
