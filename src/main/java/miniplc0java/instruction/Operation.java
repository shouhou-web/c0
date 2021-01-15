package miniplc0java.instruction;

public enum Operation {
    /**
     * 空指令
     */
    nop(0x00),
    /**
     * 将num压栈
     */
    push(0x01),
    /**
     * 弹栈一个slot
     */
    pop(0x02),
    /**
     * 弹栈num个slot
     */
    popn(0x03),
    /**
     * 复制栈顶slot
     */
    dup(0x04),
    /**
     * 加载 off 个 slot 处局部变量的地址
     */
    loca(0x0a),
    /**
     * 加载 off 个 slot 处参数/返回值的地址
     */
    arga(0x0b),
    /**
     * 加载第 n 个全局变量/常量的地址
     */
    globa(0x0c),
    /**
     * 从 addr 加载 8 位 value 压栈
     */
    load8(0x10),
    /**
     * 从 addr 加载 16 位 value 压栈
     */
    load16(0x11),
    /**
     * 从 addr 加载 32 位 value 压栈
     */
    load32(0x12),
    /**
     * 从 addr 加载 64 位 value 压栈
     */
    load64(0x13),
    /**
     * 把 val 截断到 8 位存入 addr
     */
    store8(0x14),
    /**
     * 把 val 截断到 16 位存入 addr
     */
    store16(0x15),
    /**
     * 把 val 截断到 16 位存入 addr
     */
    store32(0x16),
    /**
     * 把 val 存入 addr
     */
    store64(0x17),
    /**
     * 在堆上分配 size 字节的内存
     */
    alloc(0x18),
    /**
     * 释放 addr 指向的内存块
     */
    free(0x19),
    /**
     * 在当前栈顶分配 size 个 slot，初始化为 0
     */
    stackalloc(0x1a),
    /**
     * 计算 res = lhs + rhs，参数为整数
     */
    addi(0x20),
    /**
     * 计算 res = lhs - rhs，参数为整数
     */
    subi(0x21),
    /**
     * 计算 res = lhs * rhs，参数为整数
     */
    muli(0x22),
    /**
     * 计算 res = lhs / rhs，参数为有符号整数
     */
    divi(0x23),
    /**
     * 计算 res = lhs + rhs，参数为浮点数
     */
    addf(0x24),
    /**
     * 计算 res = lhs - rhs，参数为浮点数
     */
    subf(0x25),
    /**
     * 计算 res = lhs * rhs，参数为浮点数
     */
    mulf(0x26),
    /**
     * 计算 res = lhs / rhs，参数为浮点数
     */
    divf(0x27),
    /**
     * 计算 res = lhs / rhs，参数为无符号整数
     */
    divu(0x28),
    /**
     * 计算 res = lhs << rhs
     */
    shl(0x29),
    /**
     * 计算 res = lhs >> rhs （算术右移）
     */
    shr(0x2a),
    /**
     * 计算 res = lhs & rhs
     */
    and(0x2b),
    /**
     * 计算 res = lhs | rhs
     */
    or(0x2c),
    /**
     * 计算 res = lhs ^ rhs
     */
    xor(0x2d),
    /**
     * 计算 res = !lhs
     */
    not(0x2e),
    /**
     * 比较有符号整数 lhs 和 rhs 大小
     */
    cmpi(0x30),
    /**
     * 比较无符号整数 lhs 和 rhs 大小
     */
    cmpu(0x31),
    /**
     * 比较浮点数 lhs 和 rhs 大小
     */
    cmpf(0x32),
    /**
     * 对 lhs 取反
     */
    negi(0x34),
    /**
     * 对 lhs 取反
     */
    negf(0x35),
    /**
     * 把 lhs 从整数转换成浮点数
     */
    itof(0x36),
    /**
     * 把 lhs 从浮点数转换成整数
     */
    ftoi(0x37),
    /**
     * 计算 res = lhs >>> rhs （逻辑右移）
     */
    shrl(0x38),
    /**
     * 如果 lhs < 0 则推入 1，否则 0
     */
    setlt(0x39),
    /**
     * 如果 lhs > 0 则推入 1，否则 0
     */
    setgt(0x3a),
    /**
     * 无条件跳转偏移 off
     */
    br(0x41),
    /**
     * 如果 test 是 0 则跳转偏移 off
     */
    brfalse(0x42),
    /**
     * 如果 test 非 0 则跳转偏移 off
     */
    brtrue(0x43),
    /**
     * 调用编号为 id 的函数
     */
    call(0x48),
    /**
     * 从当前函数返回
     */
    ret(0x49),
    /**
     * 调用名称与编号为 id 的全局变量内容相同的函数
     */
    callname(0x4a),
    /**
     * 从标准输入读入一个整数 n
     */
    scani(0x50),
    /**
     * 从标准输入读入一个字符 c
     */
    scanc(0x51),
    /**
     * 从标准输入读入一个浮点数 f
     */
    scanf(0x52),
    /**
     * 向标准输出写入一个有符号整数 x
     */
    printi(0x54),
    /**
     * 向标准输出写入字符 c
     */
    printc(0x55),
    /**
     * 向标准输出写入浮点数 f
     */
    printf(0x56),
    /**
     * 向标准输出写入全局变量 i 代表的字符串
     */
    prints(0x57),
    /**
     * 向标准输出写入一个换行
     */
    println(0x58),
    /**
     * 恐慌（强行退出）
     */
    panic(0xfe);

    int opt;

    Operation(int opt) {
        this.opt = opt;
    }

    public int getOptnum() {
        return opt;
    }
}
