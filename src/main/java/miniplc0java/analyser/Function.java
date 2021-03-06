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
    ArrayList<Instruction> body = new ArrayList<Instruction>();

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
                break;
            case VOID_KW:
                this.ret_slots = 0;
                break;
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

    public int getOrder() {
        return order;
    }

    public int getLoc_slots() {
        return loc_slots;
    }

    public int getBody_count() {
        return body_count;
    }

    public int getParam_slots() {
        return param_slots;
    }

    public ArrayList<Instruction> getBody() {
        return body;
    }

    public String getName() {
        return name;
    }

    public int getRet_slots() {
        return ret_slots;
    }

    public TokenType getRet_type() {
        return ret_type;
    }

    @Override
    public String toString() {
        return "Function{" +
                "name='" + name + '\'' +
                ", order=" + order +
                ", ret_slots=" + ret_slots +
                ", ret_type=" + ret_type +
                ", param_slots=" + param_slots +
                ", loc_slots=" + loc_slots +
                ", body_count=" + body_count +
                ", body=" + body +
                '}';
    }
}
