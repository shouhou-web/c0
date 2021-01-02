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
     * 当前的符号表
     */
    SymbolTable currentTable = new SymbolTable();

    /**
     * 全局函数表
     */
    LinkedHashMap<String, Function> funcTable = new LinkedHashMap<>();

    /**
     * 当前分析的函数
     */
    Function curFunc;

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
     * @param curPos        当前 token 的位置（报错用）
     * @throws CompileError 如果重复定义了则抛异常
     */
    private void addSymbolVariable(String name, boolean isConstant, boolean isInitialized, TokenType type, Pos curPos) throws CompileError {
        // 区分全局和函数内
        if (currentTable.fatherTable == null)
            currentTable.putVariable(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.ALL, getNextVariableOffset()));
        else
            currentTable.putVariable(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.VARIABLE, getNextVariableOffset()));
    }

    private void addSymbolParam(String name, boolean isConstant, boolean isInitialized, TokenType type, Pos curPos) throws CompileError {
        currentTable.putParam(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.PARAM, getNextVariableOffset()));
    }

    /**
     * 添加一个函数
     *
     * @param name   名字
     * @param curPos 当前 func 的位置（报错用）
     * @throws CompileError 如果重复定义了则抛异常
     */
    private void addFunc(String name, Pos curPos) throws CompileError {
        if (funcTable.get(name) != null)
            throw new AnalyzeError(ErrorCode.DuplicateFunction, curPos);
        else {
            var func = new Function(name);
            // 将函数名加入变量表
            currentTable.putVariable(name, new SymbolEntry(false, false, TokenType.FUNCTION_KW, SymbolType.Function, getNextVariableOffset()));
            // 设置当前分析的函数
            curFunc = func;
            // 设置其在全局变量表中的位置
            curFunc.order = currentTable.get(name).order;
            funcTable.put(name, func);
        }
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
     * 检查函数返回值TY的，如果是则前进一位
     *
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private TokenType expectTY() throws CompileError {
        var token = peek();
        if (check(TokenType.INT_KW) || check(TokenType.VOID_KW) || check(TokenType.DOUBLE_KW)) {
            return next().getTokenType();
        } else {
            throw new ExpectedTokenError(TokenType.TY_KW, token);
        }
    }

    /**
     * 检查变量定义TY的，如果是则前进一位
     *
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private TokenType expectParam_TY() throws CompileError {
        var token = peek();
        if (check(TokenType.INT_KW) || check(TokenType.DOUBLE_KW)) {
            return next().getTokenType();
        } else {
            throw new ExpectedTokenError(TokenType.TY_KW, token);
        }
    }

    /**
     * 进入一个新的域
     *
     * @param
     */
    private void newDomain() {
        currentTable = new SymbolTable(currentTable);
    }

    /**
     * 退出域进入原本域
     *
     * @param
     */
    private void exitDomain() {
        currentTable = currentTable.fatherTable;
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

        // 加入函数表
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();
        addFunc(name, nameToken.getStartPos());

        // 进入一个新的域
        newDomain();

        // (
        expect(TokenType.L_PAREN);

        // function_param_list
        analyseFunctionParamList();

        // )->
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        // 设置类型
        curFunc.setRet_slots(expectTY());

        // block_stmt
        analyseBlock_Stmt();

        // 退出域
        exitDomain();
    }

    private void analyseFunctionParamList() throws CompileError {
        // function_param_list -> function_param (',' function_param)*

        if (check(TokenType.IDENT) || check(TokenType.CONST_KW))
            analyseFunctionParam();

        // 检查多个参数
        while (nextIf(TokenType.COMMA) != null)
            analyseFunctionParam();
    }

    private void analyseFunctionParam() throws CompileError {
        // function_param -> 'const'? IDENT ':' ty

        boolean isConstant = false;
        if (nextIf(TokenType.CONST_KW) != null) {
            // 常量
            isConstant = true;
        }
        // 分析变量名
        var nameToken = expect(TokenType.IDENT);
        String name = nameToken.getValueString();

        // :冒号
        expect(TokenType.COLON);

        // 类型
        var typeToken = expectParam_TY();

        // 加入符号表
        addSymbolParam(name, isConstant, false, typeToken, nameToken.getStartPos());

        // 增加参数个数
        curFunc.incParam_slots();
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
                newDomain();
                analyseBlock_Stmt();
                exitDomain();
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
        // todo:这个循环似乎有错？
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

        // ('=' expr)?
        boolean isInitialized = false;
        if (nextIf(TokenType.ASSIGN) != null) {
            // 分析表达式
            if (analyseExpr().type == typeToken)
                isInitialized = true;
            else
                throwError(ErrorCode.InvalidAssignment);
        }

        // 加入符号表
        addSymbolVariable(name, false, isInitialized, typeToken, nameToken.getStartPos());

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

        // '=' expr
        expect(TokenType.ASSIGN);

        // 分析表达式
        if (analyseExpr().type != typeToken)
            throwError(ErrorCode.InvalidAssignment);

        // 加入符号表
        addSymbolVariable(name, true, true, typeToken, nameToken.getStartPos());

        // ;
        expect(TokenType.SEMICOLON);
    }

    private void analyseIf_Stmt() throws CompileError {
        // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
        // { if }
        expect(TokenType.IF_KW);

        // todo:代码生成中的 if 表达式
        var expr = analyseExpr();


        // 进入前进入新的域
        newDomain();
        // 块分析
        analyseBlock_Stmt();
        // 退出域
        exitDomain();

        // ('else' 'if' expr block_stmt)*
        while (nextIf(TokenType.ELSE_KW) != null) {
            expect(TokenType.IF_KW);

            // todo:代码生成中的 else if 表达式
            analyseExpr();

            // 进入前进入新的域
            newDomain();
            // 块分析
            analyseBlock_Stmt();
            // 退出域
            exitDomain();
        }

        // ('else' block_stmt)?
        if (nextIf(TokenType.ELSE_KW) != null) {
            // 进入前进入新的域
            newDomain();
            // 块分析
            analyseBlock_Stmt();
            // 退出域
            exitDomain();
        }
    }

    private void analyseWhile_Stmt() throws CompileError {
        // while_stmt -> 'while' expr block_stmt
        //  { while }
        expect(TokenType.WHILE_KW);

        // todo:代码生成中的 while 表达式
        analyseExpr();

        // 进入前进入新的域
        newDomain();
        // 块分析
        analyseBlock_Stmt();
        // 退出域
        exitDomain();
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

        // 检查返回值类型
        var expr = analyseExpr();
        if (expr.type != curFunc.ret_type)
            throwError(ErrorCode.OtherReturnType);

        expect(TokenType.SEMICOLON);
    }

    private void analyseEmpty_Stmt() throws CompileError {
        // empty_stmt -> ';'
        // { ; }
        expect(TokenType.SEMICOLON);
    }

    private SymbolEntry analyseExpr() throws CompileError {
        // # 表达式
        // expr -> exprA
        return analyseExprA();
    }

    private SymbolEntry analyseExprA() throws CompileError {
        // A -> B ( '=' A )*
        var exprA = analyseExprB();
        // 避免连等
        if (nextIf(TokenType.ASSIGN) != null) {
            checkIfNotTemp(exprA);
            checkIdentInitialized(analyseExprA());
            // 赋值变量
            exprA.setInitialized(true);
            return new SymbolEntry(TokenType.VOID_KW);
        }
        return exprA;
    }

    private SymbolEntry analyseExprB() throws CompileError {
        // B -> C ( ( '<'|'>'|'<='|'>='|'=='|'!=' ) C )*
        var exprB = analyseExprC();
        if (check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) || check(TokenType.GE) || check(TokenType.EQ) || check(TokenType.NEQ)) {
            var token = next();
            switch (token.getTokenType()) {
                case LT:
                case GT:
                case LE:
                case GE:
                case EQ:
                case NEQ:
                    checkIdentType(analyseExprC(), exprB);
                    return new SymbolEntry(TokenType.BOOLEAN_KW);
                default:
                    // 这个语句应该不会被执行到
                    throw new ExpectedTokenError(TokenType.LT, peekedToken);
            }
        }
        return exprB;
    }

    private SymbolEntry analyseExprC() throws CompileError {
        // C -> D ( {+|-} D )*
        var exprC = analyseExprD();
        if (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            var token = next();
            switch (token.getTokenType()) {
                case PLUS:
                case MINUS:
                    // 检查类型和初始化,并返回相应type的临时变量
                    checkIdentType(analyseExprD(), exprC);
                    exprC = new SymbolEntry(true, exprC.type);
                    break;
                default:
                    throw new ExpectedTokenError(TokenType.PLUS, token);
            }
        }
        return exprC;
    }

    private SymbolEntry analyseExprD() throws CompileError {
        // D -> E ( {*|/} E )*
        var exprD = analyseExprE();
        while (check(TokenType.MUL) || check(TokenType.DIV)) {
            var token = next();
            switch (token.getTokenType()) {
                case MUL:
                case DIV:
                    // 检查类型和初始化,并返回相应type的临时变量
                    checkIdentType(analyseExprE(), exprD);
                    exprD = new SymbolEntry(true, exprD.type);
                    break;
                default:
                    throw new ExpectedTokenError(TokenType.MUL, token);
            }
        }
        return exprD;
    }

    private SymbolEntry analyseExprE() throws CompileError {
        // E -> F [ 'as' type ]
        var exprE = analyseExprF();
        if (nextIf(TokenType.AS_KW) != null) {
            var typeToken = expectParam_TY();
            if (exprE.isInitialized && (exprE.type == TokenType.INT_KW || exprE.type == TokenType.DOUBLE_KW))
                return new SymbolEntry(true, typeToken);
            else
                throwError(ErrorCode.InvalidAs);
        }
        return exprE;
    }

    private SymbolEntry analyseExprF() throws CompileError {
        // F -> [ '-' ] G
        if (nextIf(TokenType.MINUS) != null) {
            // todo:代码生成中F表达式的负号分析
        }
        return analyseExprG();
    }

    private SymbolEntry analyseExprG() throws CompileError {
        // G -> '(' A ')' | H
        if (nextIf(TokenType.L_PAREN) != null) {
            var expr = analyseExprA();
            expect(TokenType.R_PAREN);
            return expr;
        }
        return analyseExprH();
    }

    private SymbolEntry analyseExprH() throws CompileError {
        // H -> I | Ident [ '(' A (',' A)* ')' ]
        if (check(TokenType.IDENT)) {
            var nameToken = expect(TokenType.IDENT);
            String name = nameToken.getValueString();

            // 这里是函数调用
            if (nextIf(TokenType.L_PAREN) != null) {
                // 获取函数
                var func = funcTable.get(name);
                if (func == null)
                    throwError(ErrorCode.FuncNotAppear);

                // todo:这里应该是代码生成中的函数调用
                var expr = analyseExprA();
                while (nextIf(TokenType.COMMA) != null) {
                    expr = analyseExprA();
                }

                expect(TokenType.R_PAREN);

                // 函数返回
                if (func.ret_type == TokenType.VOID_KW)
                    return new SymbolEntry(TokenType.VOID_KW);
                else
                    return new SymbolEntry(true, func.ret_type);
            }

            // 获取Ident
            return currentTable.get(name);
        }
        return analyseExprI();
    }

    private SymbolEntry analyseExprI() throws CompileError {
        // I -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
        if (nextIf(TokenType.Uint_LITERAL) != null)
            return new SymbolEntry(true, TokenType.INT_KW);
        else if (nextIf(TokenType.DOUBLE_LITERAL) != null)
            return new SymbolEntry(true, TokenType.DOUBLE_KW);
        else if (nextIf(TokenType.STRING_LITEREAL) != null)
            return new SymbolEntry(true, TokenType.STRING_KW);
        else if (nextIf(TokenType.CHAR_LITEREAL) != null)
            return new SymbolEntry(true, TokenType.INT_KW);
        throw new ExpectedTokenError(TokenType.Uint_LITERAL, peekedToken);
    }

    /**
     * 检查是否是临时变量
     *
     * @param expr
     */
    private void checkIfNotTemp(SymbolEntry expr) throws CompileError {
        if (expr.symbolType == SymbolType.TEMPORARY)
            throwError(ErrorCode.AssignToTemporary);
    }

    /**
     * 检查比较两边的类型
     *
     * @param exprA
     * @param exprB
     */
    private void checkIdentType(SymbolEntry exprA, SymbolEntry exprB) throws CompileError {
        checkIdentInitialized(exprA);
        checkIdentInitialized(exprB);
        if (exprA.type != exprB.type) {
            throwError(ErrorCode.InvalidCalculation);
        }
    }

    /**
     * 检查变量初始化
     *
     * @param expr
     */
    private void checkIdentInitialized(SymbolEntry expr) throws CompileError {
        if (!expr.isInitialized)
            throwError(ErrorCode.NotInitialized);
    }

    private void throwError(ErrorCode error) throws CompileError {
        throw new CompileError() {
            @Override
            public ErrorCode getErr() {
                return error;
            }

            @Override
            public Pos getPos() {
                return null;
            }
        };
    }
}
