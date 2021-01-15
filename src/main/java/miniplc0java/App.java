package miniplc0java;

public class App {
    public static void main(String[] args) {
        System.out.println(toFullBinaryString(32, 8));
        ;
    }

    public static String toFullBinaryString(int num, int size) {
        char[] chs = new char[size];
        for (int i = 0; i < size; i++)
            chs[size - 1 - i] = (char) (((num >> i) & 1) + '0');
        return new String(chs);
    }
}
