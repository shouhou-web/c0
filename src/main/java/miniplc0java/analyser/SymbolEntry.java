package miniplc0java.analyser;

public class SymbolEntry {
    boolean isConstant;
    boolean isInitialized;
    boolean isFunction; // 是否是函数名
    int stackOffset;

    /**
     * @param isConstant
     * @param isDeclared
     * @param stackOffset
     */
    public SymbolEntry(boolean isConstant, boolean isDeclared, boolean isFunction, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.isFunction = isFunction;
        this.stackOffset = stackOffset;
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
