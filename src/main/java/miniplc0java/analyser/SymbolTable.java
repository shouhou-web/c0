package miniplc0java.analyser;

import java.util.LinkedHashMap;

public class SymbolTable {
    // father table
    SymbolTable fatherTable = null;
    // current table
    LinkedHashMap<String, SymbolEntry> symbolTable = new LinkedHashMap<>();

    // init 设置fatherTable为null
    public SymbolTable() {
        fatherTable = null;
    }

    // init 设置fatherTable指向父域
    public SymbolTable(SymbolTable currentTable) {
        fatherTable = currentTable;
    }

    // 添加表
    public void addTable(String name, SymbolEntry entry) {
        if (symbolTable.get(name) != null)
            throw new Error("变量名重复");
        else
            symbolTable.put(name,entry);
    }

    // 查找表
    public SymbolEntry findParam(String name) {
        SymbolTable find = this;
        while (find.symbolTable.get(name) == null && find.fatherTable != null ) {
            find = find.fatherTable;
        }
        return find.symbolTable.get(name);
    }


}
