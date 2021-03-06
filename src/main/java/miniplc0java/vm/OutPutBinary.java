package miniplc0java.vm;

import miniplc0java.*;
import miniplc0java.analyser.Function;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.SymbolTable;
import miniplc0java.analyser.SymbolType;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;

import java.nio.ByteBuffer;
import java.util.*;

public class OutPutBinary {
    SymbolTable symbolTable;
    LinkedHashMap<String, Function> functionTables;
    List<Byte> output;

    int magic = 0x72303b3e;
    int version = 0x00000001;

    public OutPutBinary(SymbolTable table, LinkedHashMap<String, Function> funcTable) {
        this.symbolTable = table;
        this.functionTables = funcTable;
        output = new ArrayList<>();
    }

    public List<Byte> generate() {
        // 魔数
        List<Byte> magic = int2bytes(4, this.magic);
        output.addAll(magic);

        // 版本号
        List<Byte> version = int2bytes(4, this.version);
        output.addAll(version);

        // 全局变量表
        LinkedHashMap<String, SymbolEntry> globals = symbolTable.getSymbolTable();

        List<Byte> globalCount = int2bytes(4, globals.size());
        output.addAll(globalCount);

        for (Map.Entry<String, SymbolEntry> stringSymbolEntryEntry : globals.entrySet()) {
            var entry = (Map.Entry) stringSymbolEntryEntry;
            SymbolEntry oneGlobalSymbol = (SymbolEntry) entry.getValue();
//            System.out.println(oneGlobalSymbol.toString());
            List<Byte> globalIsConst;
            if (oneGlobalSymbol.isConstant())
                globalIsConst = int2bytes(1, 1);
            else
                globalIsConst = int2bytes(1, 0);

            output.addAll(globalIsConst);

            List<Byte> globalValue = getValueByte(oneGlobalSymbol);

            List<Byte> globalValueCount = int2bytes(4, globalValue.size());
            output.addAll(globalValueCount);

            output.addAll(globalValue);
        }

        // 函数列表

        List<Byte> functions_count = int2bytes(4, functionTables.size());
        output.addAll(functions_count);

        for (Map.Entry<String, Function> stringSymbolEntryEntry : functionTables.entrySet()) {
            var entry = (Map.Entry) stringSymbolEntryEntry;
            Function oneFunction = (Function) entry.getValue();
//            System.out.println(oneFunction.toString());
            // 函数名
            List<Byte> name = int2bytes(4, oneFunction.getOrder());
            output.addAll(name);

            // 返回值
            List<Byte> retSlots = int2bytes(4, oneFunction.getRet_slots());
            output.addAll(retSlots);

            // 参数
            List<Byte> paramsSlots = int2bytes(4, oneFunction.getParam_slots());
            output.addAll(paramsSlots);

            // 局部变量
            List<Byte> locSlots = int2bytes(4, oneFunction.getLoc_slots());
            output.addAll(locSlots);

            // 函数体指令
            ArrayList<Instruction> instructions = oneFunction.getBody();
            List<Byte> bodyCount = int2bytes(4, instructions.size());
            output.addAll(bodyCount);

            // 指令集
            for (Instruction instruction : instructions) {
                // type
                Operation opt = instruction.getOpt();
//                System.out.println(opt.getOptnum());
                List<Byte> type = int2bytes(1, opt.getOptnum());
                output.addAll(type);

//                System.out.println(instruction.toString());
                if (instruction.getType() > 0) {
                    List<Byte> x;
//                    System.out.println(instruction.getX());
                    if (instruction.getType() == 1)
                        x = int2bytes(4, (int) instruction.getX());
                    else {
                        if (instruction.getX().getClass() == Double.class)
                            x = handleDouble((Double) instruction.getX());
                        else
                            x = long2bytes(8, (long) instruction.getX());
                    }
                    output.addAll(x);
                }
            }
        }
        return output;
    }

    private List<Byte> getValueByte(SymbolEntry globalSymbol) {
        List<Byte> bytes = new ArrayList<>();

        if (globalSymbol.getSymbolType() == SymbolType.Function || globalSymbol.getSymbolType() == SymbolType.GLOBAL_STRING)
            bytes = String2bytes(globalSymbol.getValue());
        else
            bytes = long2bytes(8, 0);
        return bytes;
    }

    private List<Byte> Char2bytes(char value) {
        List<Byte> AB = new ArrayList<>();
        AB.add((byte) (value & 0xff));
        return AB;
    }

    private List<Byte> String2bytes(String valueString) {
        List<Byte> AB = new ArrayList<>();
        for (int i = 0; i < valueString.length(); i++) {
            char ch = valueString.charAt(i);
            AB.add((byte) (ch & 0xff));
        }
        return AB;
    }

    private List<Byte> long2bytes(int length, long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length - 1);
        for (int i = 0; i < length; i++) {
            bytes.add((byte) ((target >> (start - i * 8)) & 0xFF));
        }
        return bytes;
    }

    public ArrayList<Byte> handleDouble(double num) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(0, num);
        ArrayList<Byte> res = new ArrayList<>();
        for (byte b : buffer.array()) {
            res.add(b);
        }
        return res;
    }

    private ArrayList<Byte> int2bytes(int length, int target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length - 1);
        for (int i = 0; i < length; i++) {
            bytes.add((byte) ((target >> (start - i * 8)) & 0xFF));
        }
        return bytes;
    }

//    public static ArrayList<Byte> int2bytes(int num) {
//        ByteBuffer buffer = ByteBuffer.allocate(4);
//        buffer.putInt(0, num);
//        ArrayList<Byte> res = new ArrayList<>();
//        for (byte b : buffer.array()) {
//            res.add(b);
//        }
//        return res;
//    }
}
