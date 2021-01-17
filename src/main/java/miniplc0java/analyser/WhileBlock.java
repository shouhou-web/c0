package miniplc0java.analyser;

public class WhileBlock {
    int startOffset;
    int endOffset;

    WhileBlock(int startOffset,int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public int getStartOffset(int current) {
        return startOffset - current ;
    }

    public int getEndOffset(int current) {
        return endOffset - current;
    }
}
