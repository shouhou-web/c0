package miniplc0java.vm;

import miniplc0java.analyser.Function;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.SymbolTable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class output {
    String magic;
    String version;
    // 变量表
    SymbolTable symbolTable;
    // 全局函数表
    LinkedHashMap<String, Function> funcTable;

    public output(SymbolTable globalTable, LinkedHashMap<String, Function> functionTables) {
        this.magic = "72303b3e";
        this.version = "00000001";
        this.symbolTable = globalTable;
        this.funcTable = functionTables;
    }

    public String toVmCode() {

    }
}
