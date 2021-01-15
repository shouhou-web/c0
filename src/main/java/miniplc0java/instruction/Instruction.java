package miniplc0java.instruction;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    int type; // 0无参数，1代表u32的参数，2代表u64的参数
    Object x;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.type = 0;
        this.x = null;
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.type = 1;
        this.x = x;
    }

    public Instruction(Operation opt, Long x) {
        this.opt = opt;
        this.type = 2;
        this.x = x;
    }

    public Instruction(Operation opt, Double x) {
        this.opt = opt;
        this.type = 2;
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Object getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Override
    public String toString() {
        switch (this.opt) {
            default:
                return "ILL";
        }
    }
}
