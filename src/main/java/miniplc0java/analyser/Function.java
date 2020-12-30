package miniplc0java.analyser;

import miniplc0java.tokenizer.TokenType;

public class Function {
    String name;
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
    // body.items

    /**
     * @param Name 函数名
     */
    public Function(String Name) {
        name = Name;
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
     * @param locSlots 局部变量个数
     */
    public void setLoc_slots(int locSlots) {
        loc_slots = locSlots;
    }
}
