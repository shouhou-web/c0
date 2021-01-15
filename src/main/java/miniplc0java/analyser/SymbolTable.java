package miniplc0java.analyser;

import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

import java.util.LinkedHashMap;

public class SymbolTable {
    // father table
    SymbolTable fatherTable = null;

    // current table
    LinkedHashMap<String, SymbolEntry> symbolTable = new LinkedHashMap<>();

    // 参数的顺序
    int paramOrder = 1;

    // 变量的顺序
    int variableOrder = 0;

    // init 设置fatherTable为null
    public SymbolTable() {
        fatherTable = null;
    }

    // init 设置fatherTable指向父域
    public SymbolTable(SymbolTable currentTable) {
        fatherTable = currentTable;
    }

    // 添加参数
    public void putParam(String name, SymbolEntry entry) throws CompileError {
        checkDuplicateDeclaration(name);
        entry.order = ++paramOrder;
        symbolTable.put(name, entry);
    }

    // 添加变量
    public SymbolEntry putVariable(String name, SymbolEntry entry) throws CompileError {
        checkDuplicateDeclaration(name);
        entry.order = ++variableOrder;
        symbolTable.put(name, entry);
        return entry;
    }

    private void checkDuplicateDeclaration(String name) throws CompileError {
        if (symbolTable.get(name) != null)
            throw new CompileError() {
                @Override
                public ErrorCode getErr() {
                    return ErrorCode.DuplicateDeclaration;
                }

                @Override
                public Pos getPos() {
                    return null;
                }
            };
    }

    // 查找表
    public SymbolEntry get(String name) throws CompileError {
        SymbolTable find = this;
        while (find.symbolTable.get(name) == null && find.fatherTable != null) {
            find = find.fatherTable;
        }
        if (find.symbolTable.get(name) == null)
            throw new CompileError() {
                @Override
                public ErrorCode getErr() {
                    return ErrorCode.NotDefined;
                }

                @Override
                public Pos getPos() {
                    return null;
                }
            };
        return find.symbolTable.get(name);
    }
}
