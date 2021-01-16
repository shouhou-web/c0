package miniplc0java.analyser;

import miniplc0java.tokenizer.TokenType;

import java.util.LinkedHashMap;


public class SymbolEntry {
    boolean isConstant;
    boolean isInitialized;
    SymbolType symbolType;
    TokenType type; // int string
    int order; // 在全局或函数中的顺序
    int stackOffset;
    String value; // 专门存储string的值

    /**
     * @param isConstant
     * @param isDeclared
     */
    public SymbolEntry(boolean isConstant, boolean isDeclared, TokenType type, SymbolType symbolType) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.type = type;
        this.symbolType = symbolType;
    }

    /**
     * 存储string
     *
     * @param isConstant
     * @param isDeclared
     */
    public SymbolEntry(boolean isConstant, boolean isDeclared, TokenType type, SymbolType symbolType, String stringValue) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.type = type;
        this.symbolType = symbolType;
        this.value = stringValue;
    }

    // 第一种种临时变量储存方式
    public SymbolEntry(boolean isInitialized, TokenType type) {
        // 初始化的，包括int,string,double
        this.isInitialized = isInitialized;
        this.type = type;
        this.symbolType = SymbolType.TEMPORARY;
    }

    // 第二种临时变量储存方式
    public SymbolEntry(TokenType type) {
        // 未初始化的，包括void,boolean
        this.isInitialized = false;
        this.type = type;
        this.symbolType = SymbolType.TEMPORARY;
    }

    public String toVmCode() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isConstant)
            stringBuilder.append("00000001");
        else
            stringBuilder.append("00000000");
        System.out.println(symbolType);
        if (symbolType != SymbolType.GLOBAL_STRING)
            stringBuilder.append("00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000");
        else {
            char[] value = this.value.toCharArray();
            for (char c : value) {
                System.out.println(toFullBinaryString(c, 8));
                stringBuilder.append(toFullBinaryString(c, 8));
            }
        }
        return stringBuilder.toString();
    }

    public static String toFullBinaryString(int num, int size) {
        char[] chs = new char[size];
        for (int i = 0; i < size; i++)
            chs[size - 1 - i] = (char) (((num >> i) & 1) + '0');
        return new String(chs);
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public String getValue() {
        return value;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }
}
