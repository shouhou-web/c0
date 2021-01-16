package miniplc0java.vm;

import miniplc0java.analyser.Function;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.SymbolTable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Output {
    String magic;
    String version;
    // 变量表
    SymbolTable symbolTable;
    // 全局函数表
    LinkedHashMap<String, Function> funcTable;

    public Output(SymbolTable globalTable, LinkedHashMap<String, Function> functionTables) {
        this.magic = "72303b3e";
        this.version = "00000001";
        this.symbolTable = globalTable;
        this.funcTable = functionTables;
    }

    public String toVmCode() {
        StringBuilder stringBuilder = new StringBuilder("");
        // 魔数
        stringBuilder.append(toFullBinaryString(Integer.parseInt(magic,16),32));
        stringBuilder.append(toFullBinaryString(Integer.parseInt(version,16),32));
        // 全局变量
        String globals = symbolTable.toVmCode();
        stringBuilder.append(toFullBinaryString(symbolTable.getSize(),32));
        stringBuilder.append(globals);
        // 全局函数
        int func_count = 0;
        Iterator iter = funcTable.entrySet().iterator();
        while (iter.hasNext()) {
            func_count += 1;
            var entry = (Map.Entry) iter.next();
            Function symbolEntry = (Function) entry.getValue();
            stringBuilder.append(symbolEntry.toVmCode());
        }
        return stringBuilder.toString();
    }

    public static String toFullBinaryString(int num, int size) {
        char[] chs = new char[size];
        for (int i = 0; i < size; i++)
            chs[size - 1 - i] = (char) (((num >> i) & 1) + '0');
        return new String(chs);
    }
}
