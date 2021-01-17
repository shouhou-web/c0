package miniplc0java.analyser;

import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    // father table
    SymbolTable fatherTable = null;

    // current table
    LinkedHashMap<String, SymbolEntry> symbolTable = new LinkedHashMap<>();

    // 参数的顺序
    int paramOrder = 1;

    // 变量的顺序
    int variableOrder = 0;

    // 变量表大小
    int size;

    // init 设置fatherTable为null
    public SymbolTable() {
        fatherTable = null;
    }

    // init 设置fatherTable指向父域
    public SymbolTable(SymbolTable currentTable) {
        fatherTable = currentTable;
        if (currentTable.fatherTable != null)
            variableOrder = currentTable.variableOrder;
    }

    // 添加参数
    public void putParam(String name, SymbolEntry entry) throws CompileError {
        checkDuplicateDeclaration(name);
        entry.order = paramOrder++;
        symbolTable.put(name, entry);
    }

    // 添加变量
    public SymbolEntry putVariable(String name, SymbolEntry entry) throws CompileError {
        SymbolTable find = this;
        checkDuplicateDeclaration(name);
        entry.order = variableOrder++;
        symbolTable.put(name, entry);
        return entry;
    }

    // 添加全局string
    public SymbolEntry putGlobalString(SymbolEntry entry) throws CompileError {
        SymbolTable find = this;
        while (find.fatherTable != null) {
            find = find.fatherTable;
        }
        entry.order = find.variableOrder++;
        find.getSymbolTable().put(entry.getValue() + entry.order, entry);
        return entry;
    }

    // 添加全局函数
    public SymbolEntry putGlobalFunc(String name, SymbolEntry entry) throws CompileError {
        SymbolTable find = this;
        while (find.fatherTable != null) {
            find = find.fatherTable;
        }
        entry.order = find.variableOrder++;
        find.getSymbolTable().put(name, entry);
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

    // 获取大小
    public int getSize() {
        return size;
    }

    public LinkedHashMap<String, SymbolEntry> getSymbolTable() {
        return symbolTable;
    }
}
