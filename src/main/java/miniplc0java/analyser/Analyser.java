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
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * 当前的符号表
     */
    public SymbolTable currentTable = new SymbolTable();

    /**
     * 全局函数表
     */
    public LinkedHashMap<String, Function> funcTable = new LinkedHashMap<>();

    /**
     * 当前分析的函数
     */
    Function curFunc;


    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public LinkedHashMap<String, Function> analyse() throws CompileError {
        analyseProgram();
        return funcTable;
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
    private SymbolEntry addSymbolVariable(String name, boolean isConstant, boolean isInitialized, TokenType type, Pos curPos) throws CompileError {
        // 区分全局和函数内
        if (currentTable.fatherTable == null)
            return currentTable.putVariable(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.ALL));
        else
            return currentTable.putVariable(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.VARIABLE));
    }

    private void addSymbolParam(String name, boolean isConstant, boolean isInitialized, TokenType type, Pos curPos) throws CompileError {
        currentTable.putParam(name, new SymbolEntry(isConstant, isInitialized, type, SymbolType.PARAM));
    }

    private SymbolEntry addGlobalSymbolVariable(TokenType type) throws CompileError {
        // 区分全局和函数内
        return currentTable.putGlobalVariable(new SymbolEntry(false, false, type, SymbolType.GLOBAL_STRING));
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
            currentTable.putVariable(name, new SymbolEntry(false, false, TokenType.FUNCTION_KW, SymbolType.Function));
            // 设置当前分析的函数
            curFunc = func;
            // 设置其在全局变量表中的位置
            curFunc.setOrder(currentTable.get(name).order);
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
     * 添加指令
     *
     * @param opt
     * @param x
     */
    public Instruction addInstruction(Operation opt, Integer x) {
        Instruction instruction = new Instruction(opt, x);
        curFunc.body.add(instruction);
        // 增加指令数
        curFunc.incBody_count();
        return instruction;
    }

    public void addInstruction(Operation opt) {
        this.curFunc.body.add(new Instruction(opt));
    }

    public void addInstruction(Operation opt, Long x) {
        this.curFunc.body.add(new Instruction(opt, x));
    }

    public void addInstruction(Operation opt, Double x) {
        this.curFunc.body.add(new Instruction(opt, x));
    }

    /**
     * 移除指令
     *
     * @param
     */
    public void popInstruction() {
        this.curFunc.body.remove(this.curFunc.body.size() - 1);
    }

    /**
     * 获取当前偏移
     *
     * @param
     */
    public int getInstructionOffset() {
        return this.curFunc.body.size();
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
        else
            return;

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

        // 增加局部变量数
        curFunc.incLoc_slots();
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

        // 加入符号表
        SymbolEntry entry = addSymbolVariable(name, false, false, typeToken, nameToken.getStartPos());

        // ('=' expr)?
        if (nextIf(TokenType.ASSIGN) != null) {
            // 加载地址
            if (entry.symbolType == SymbolType.ALL)
                addInstruction(Operation.globa, entry.order);
            else if (entry.symbolType == SymbolType.PARAM)
                addInstruction(Operation.loca, entry.order);

            // 分析表达式
            if (analyseExpr().type == typeToken) {
                // 设置变量初始化
                entry.setInitialized(true);
                // 存储变量
                addInstruction(Operation.store64);
            } else
                throwError(ErrorCode.InvalidAssignment);
        }

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

        // 加入符号表
        SymbolEntry entry = addSymbolVariable(name, true, true, typeToken, nameToken.getStartPos());

        // 加载地址
        if (entry.symbolType == SymbolType.ALL)
            addInstruction(Operation.globa, entry.order);
        else if (entry.symbolType == SymbolType.PARAM)
            addInstruction(Operation.loca, entry.order);

        // '=' expr
        expect(TokenType.ASSIGN);

        // 分析表达式
        if (analyseExpr().type != typeToken)
            throwError(ErrorCode.InvalidAssignment);

        // 存入变量
        addInstruction(Operation.store64);

        // ;
        expect(TokenType.SEMICOLON);
    }

    private void analyseIf_Stmt() throws CompileError {
        // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
        // { if }
        expect(TokenType.IF_KW);

        // 分析表达式，将值压栈
        var expr = analyseExpr();

        // 预备的跳转指令，当条件为false则跳过block
        // x将在分析完block时计算
        Instruction br1 = addInstruction(Operation.brfalse, 0);
        int start = getInstructionOffset();

        // 进入前进入新的域
        newDomain();
        // 块分析
        analyseBlock_Stmt();
        // 退出域
        exitDomain();

        // 无跳转其他块
        Instruction br2 = addInstruction(Operation.br, 0);
        int middle = getInstructionOffset();
        // 设置跳转至该位置
        br1.setX(middle - start);

        if (nextIf(TokenType.ELSE_KW) != null) {
            // ('else' 'if' expr block_stmt)*
            if (check(TokenType.IF_KW))
                analyseIf_Stmt();

                // ('else' block_stmt)?
            else if (check(TokenType.L_BRACE)) {
                // 进入前进入新的域
                newDomain();
                // 块分析
                analyseBlock_Stmt();
                // 退出域
                exitDomain();
            }
        }

        // 出来的怎么办？直接跳过所有
        int end = getInstructionOffset();
        br2.setX(end);

    }

    private void analyseWhile_Stmt() throws CompileError {
        // while_stmt -> 'while' expr block_stmt
        //  { while }
        expect(TokenType.WHILE_KW);

        // 设置开始地址
        int start = getInstructionOffset();

        // 代码生成中的 while 表达式，push状态
        analyseExpr();
        Instruction br1 = addInstruction(Operation.brfalse, 0);
        int middle = getInstructionOffset();

        // 进入前进入新的域
        newDomain();
        // 块分析
        analyseBlock_Stmt();
        // 退出域
        exitDomain();

        // 跳回原地址
        Instruction br2 = addInstruction(Operation.br, 0);
        int end = getInstructionOffset();

        // 跳过while块
        br1.setX(end - middle);

        // 循环while
        br2.setX(start - end);
    }

    private void analyseBreak_Stmt() throws CompileError {
        // break_stmt -> 'break' ';'
        // { break }
        // todo:【加分】break的代码生成
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
    }

    private void analyseContinue_Stmt() throws CompileError {
        // continue_stmt -> 'continue' ';'
        // { continue }
        // todo:【加分】continue的代码生成
        expect(TokenType.CONTINUE_KW);
        expect(TokenType.SEMICOLON);
    }

    private void analyseReturn_Stmt() throws CompileError {
        // return_stmt -> 'return' expr? ';'
        // { return }
        expect(TokenType.RETURN_KW);

        if (!check(TokenType.SEMICOLON)) {
            // 加载返回值地址
            addInstruction(Operation.arga, 0);

            var expr = analyseExpr();
            // 检查返回值类型
            if (expr.type != curFunc.ret_type)
                throwError(ErrorCode.OtherReturnType);

            // 存储函数返回值
            addInstruction(Operation.store64);
        }
        // 检查返回值类型
        else if (curFunc.ret_type != TokenType.VOID_KW)
            throwError(ErrorCode.OtherReturnType);

        // ;
        expect(TokenType.SEMICOLON);
    }

    private void analyseEmpty_Stmt() throws CompileError {
        // empty_stmt -> ';'
        // { ; }
        expect(TokenType.SEMICOLON);
    }

    private SymbolEntry analyseExpr() throws CompileError {
        // 表达式
        // expr -> exprA
        return analyseExprA();
    }

    private SymbolEntry analyseExprA() throws CompileError {
        // A -> B ( '=' A )*
        var exprA = analyseExprB();
        // 避免连等
        if (nextIf(TokenType.ASSIGN) != null) {
            // todo:删除指令
            popInstruction();

            checkIfNotTemp(exprA);
            checkIdentInitialized(analyseExprA());

            // todo:没看懂
            addInstruction(Operation.store64);

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

            // 检查类型并获取下一个表达式内容
            checkIdentType(analyseExprC(), exprB);

            // 设置比较函数
            if (exprB.type == TokenType.INT_KW)
                addInstruction(Operation.cmpi);
            else if (exprB.type == TokenType.DOUBLE_KW)
                addInstruction(Operation.cmpf);

            switch (token.getTokenType()) {
                case LT:
                    addInstruction(Operation.setlt);
                    break;
                case GT:
                    addInstruction(Operation.setgt);
                    break;
                case LE:
                    // 先大于再取反
                    addInstruction(Operation.setgt);
                    addInstruction(Operation.not);
                    break;
                case GE:
                    // 先小于再取反
                    addInstruction(Operation.setlt);
                    addInstruction(Operation.not);
                    break;
                case EQ:
                    addInstruction(Operation.not);
                    break;
                case NEQ:
                    break;
                default:
                    // 这个语句应该不会被执行到
                    throw new ExpectedTokenError(TokenType.LT, peekedToken);
            }
            return new SymbolEntry(TokenType.BOOLEAN_KW);
        }
        return exprB;
    }

    private SymbolEntry analyseExprC() throws CompileError {
        // C -> D ( {+|-} D )*
        var exprC = analyseExprD();
        if (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            var token = next();

            // 检查类型和初始化,并返回相应type的临时变量
            checkIdentType(analyseExprD(), exprC);

            switch (token.getTokenType()) {
                case PLUS:
                    if (exprC.type == TokenType.INT_KW)
                        addInstruction(Operation.addi);
                    else
                        addInstruction(Operation.addf);
                    break;
                case MINUS:
                    if (exprC.type == TokenType.INT_KW)
                        addInstruction(Operation.subi);
                    else
                        addInstruction(Operation.subf);
                    break;
                default:
                    throw new ExpectedTokenError(TokenType.PLUS, token);
            }
            // 返回
            exprC = new SymbolEntry(true, exprC.type);
        }
        return exprC;
    }

    private SymbolEntry analyseExprD() throws CompileError {
        // D -> E ( {*|/} E )*
        var exprD = analyseExprE();
        while (check(TokenType.MUL) || check(TokenType.DIV)) {

            var token = next();

            // 检查类型和初始化,并返回相应type的临时变量
            checkIdentType(analyseExprE(), exprD);

            switch (token.getTokenType()) {
                case MUL:
                    if (exprD.type == TokenType.INT_KW)
                        addInstruction(Operation.muli);
                    else
                        addInstruction(Operation.mulf);
                    break;
                case DIV:
                    if (exprD.type == TokenType.INT_KW)
                        addInstruction(Operation.divi);
                    else
                        addInstruction(Operation.divf);
                    break;
                default:
                    throw new ExpectedTokenError(TokenType.MUL, token);
            }
            exprD = new SymbolEntry(true, exprD.type);
        }
        return exprD;
    }

    private SymbolEntry analyseExprE() throws CompileError {
        // E -> F [ 'as' type ]
        var exprE = analyseExprF();
        if (nextIf(TokenType.AS_KW) != null) {
            var typeToken = expectParam_TY();

            if (exprE.isInitialized && exprE.type == TokenType.INT_KW && typeToken == TokenType.DOUBLE_KW)
                addInstruction(Operation.itof);
            else if (exprE.isInitialized && exprE.type == TokenType.DOUBLE_KW && typeToken == TokenType.INT_KW)
                addInstruction(Operation.ftoi);
            else
                throwError(ErrorCode.InvalidAs);
            return new SymbolEntry(true, typeToken);
        }
        return exprE;
    }

    private SymbolEntry analyseExprF() throws CompileError {
        // F -> [ '-' ] G
        boolean isNegative = false;
        if (nextIf(TokenType.MINUS) != null) {
            // 负号分析
            isNegative = true;
        }
        SymbolEntry exprF = analyseExprG();
        if (isNegative)
            if (!exprF.isInitialized)
                throwError(ErrorCode.NotInitialized);
            else if (exprF.type == TokenType.INT_KW)
                addInstruction(Operation.negi);
            else if (exprF.type == TokenType.DOUBLE_KW)
                addInstruction(Operation.negf);
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

            // 是否是函数
            if (nextIf(TokenType.L_PAREN) != null) {
                // 获取函数
                var func = funcTable.get(name);
//                System.out.println(name);
                if (func == null && !isStdFunc(name))
                    throwError(ErrorCode.FuncNotExist);
                else if (isStdFunc(name)) {
                    if (nextIf(TokenType.R_PAREN) == null) {
                        // 分析传参
                        var expr = analyseExprA();
                        while (nextIf(TokenType.COMMA) != null) {
                            expr = analyseExprA();
                        }
                        expect(TokenType.R_PAREN);
                    }
                    return new SymbolEntry(analyseStdFunc(name));
                } else {
                    // 分配空间
                    addInstruction(Operation.stackalloc, 1);
                    if (nextIf(TokenType.R_PAREN) == null) {
                        // 分析传参
                        var expr = analyseExprA();
                        while (nextIf(TokenType.COMMA) != null) {
                            expr = analyseExprA();
                        }
                        expect(TokenType.R_PAREN);
                        addInstruction(Operation.callname, this.funcTable.get(name).order);
                    }
                    // 函数返回
                    if (func.ret_type == TokenType.VOID_KW)
                        return new SymbolEntry(TokenType.VOID_KW);
                    else
                        return new SymbolEntry(true, func.ret_type);
                }
            } else {
                // 获取Ident
                SymbolEntry entry = currentTable.get(name);
                if (entry == null)
                    throwError(ErrorCode.ParamNotExist);
                else if (entry.symbolType == SymbolType.ALL)
                    addInstruction(Operation.globa, entry.order);
                else if (entry.symbolType == SymbolType.PARAM)
                    addInstruction(Operation.arga, entry.order);
                else if (entry.symbolType == SymbolType.VARIABLE)
                    addInstruction(Operation.loca, entry.order);
                addInstruction(Operation.load64);
                return entry;
            }
        }
        return analyseExprI();
    }

    private SymbolEntry analyseExprI() throws CompileError {
        // I -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
        var nameToken = peek();
        TokenType tt = nameToken.getTokenType();
        System.out.println(tt.toString());
        if (tt == TokenType.Uint_LITERAL || tt == TokenType.CHAR_LITEREAL) {
            addInstruction(Operation.push, (long) nameToken.getValue());
            return new SymbolEntry(true, TokenType.INT_KW);
        } else if (nextIf(TokenType.DOUBLE_LITERAL) != null) {
            addInstruction(Operation.push, (Double) nameToken.getValue());
            return new SymbolEntry(true, TokenType.DOUBLE_KW);
        } else if (nextIf(TokenType.STRING_LITEREAL) != null) {
            // todo:String还没写
//            addGlobalSymbolVariable();
            addInstruction(Operation.push, (Double) nameToken.getValue());
            return new SymbolEntry(true, TokenType.STRING_KW);
        }
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
     * @param expr 检查是否赋值
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

    /**
     * 检查是否是标准库函数
     *
     * @param name 函数名
     */
    private boolean isStdFunc(String name) {
        return name.equals("getint") || name.equals("getdouble") || name.equals("getchar") ||
                name.equals("putint") || name.equals("putdouble") || name.equals("putchar") ||
                name.equals("putstr") || name.equals("putln");
    }

    /**
     * 根据标准库函数生成指令并返回类型
     *
     * @param name 函数名
     * @return Tokentype
     */
    private TokenType analyseStdFunc(String name) {
        switch (name) {
            case "getint":
                addInstruction(Operation.scani);
                return TokenType.INT_KW;
            case "getdouble":
                addInstruction(Operation.scanf);
                return TokenType.DOUBLE_KW;
            case "getchar":
                addInstruction(Operation.scanc);
                return TokenType.INT_KW;
            case "putint":
                addInstruction(Operation.printi);
                return TokenType.VOID_KW;
            case "putdouble":
                addInstruction(Operation.printf);
                return TokenType.VOID_KW;
            case "putchar":
                addInstruction(Operation.printc);
                return TokenType.VOID_KW;
            case "putln":
                addInstruction(Operation.println);
                return TokenType.VOID_KW;
            case "putstr":
                addInstruction(Operation.prints);
                return TokenType.VOID_KW;
            default:
                return TokenType.VOID_KW;
        }
    }

}
