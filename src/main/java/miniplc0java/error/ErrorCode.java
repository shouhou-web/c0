package miniplc0java.error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError, EOF, InvalidInput, InvalidIdentifier, IntegerOverflow, // int32_t overflow.
    NoBegin, NoEnd, NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    AssignToConstant, DuplicateDeclaration,  InvalidAssignment, InvalidPrint, ExpectedToken,
    NotDefined, // 变量未定义
    DuplicateFunction, // 函数重复声明
    OtherReturnType, // 返回值类型不对
    InvalidCalculation, // 非法运算（两边类型不同）
    AssignToTemporary, // 给临时变量赋值
    InvalidAs, // 转换了非int 或 double的
    FuncNotExist, // 函数不存在
    ParamNotExist, // 变量不存在
    NotInitialized, // 未定义
    NotWhileDomain,
}
