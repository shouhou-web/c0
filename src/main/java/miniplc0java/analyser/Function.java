package miniplc0java.analyser;

import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.TokenType;

import java.util.ArrayList;

public class Function {
    String name;
    // 在全局变量表中占得位置
    int order;
    // 返回值占的节点数
    int ret_slots = 0;
    // 返回值类型
    TokenType ret_type;
    // 参数占得节点数
    int param_slots = 0;
    // 局部变量所占节点数
    int loc_slots = 0;
    // 指令数目
    int body_count;
    // 指令集合
    ArrayList<Instruction> body;

    /**
     * @param name 函数名
     */
    public Function(String name) {
        this.name = name;
    }

    /**
     * @param type 返回类型
     */
    public void setRet_slots(TokenType type) {
        ret_type = type;
        switch (type) {
            case INT_KW:
            case DOUBLE_KW:
                this.ret_slots = 1;
            case VOID_KW:
                this.ret_slots = 0;
        }
    }

    /**
     * @param
     */
    public void incParam_slots() {
        param_slots += 1;
    }

    /**
     * 增加局部变量数
     *
     * @param
     */
    public void incLoc_slots() {
        loc_slots += 1;
    }

    /**
     * 增加指令数
     *
     * @param
     */
    public void incBody_count() {
        body_count += 1;
    }

    /**
     * @param order 函数在全局变量表中的位置
     */
    public void setOrder(int order) {
        this.order = order;
    }


    public String toVmCode() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(toFullBinaryString(order));
        stringBuilder.append(toFullBinaryString(ret_slots));
        stringBuilder.append(toFullBinaryString(param_slots));
        stringBuilder.append(toFullBinaryString(loc_slots));
        stringBuilder.append(toFullBinaryString(body_count));
        for (Instruction instruction : body
        ) {
            stringBuilder.append(instruction.toVmCode());
        }
        return stringBuilder.toString();
    }

    //将整数num转化为32位的二进制数
    public static String toFullBinaryString(int num) {
        char[] chs = new char[Integer.SIZE];
        for (int i = 0; i < Integer.SIZE; i++)
            chs[Integer.SIZE - 1 - i] = (char) (((num >> i) & 1) + '0');
        return new String(chs);
    }
}
