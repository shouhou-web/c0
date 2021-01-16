package miniplc0java;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.SymbolTable;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.vm.OutPutBinary;
import miniplc0java.vm.Output;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class AnalyserTest {


    @Test
    public void test() throws Exception {
        InputStream input;
        String inputFileName = "D:\\北京航空航天大学\\大三学习资料\\编译原理\\c0\\src\\main\\java\\miniplc0java\\in.txt";
        String outputFileName = "D:\\北京航空航天大学\\大三学习资料\\编译原理\\c0\\src\\main\\java\\miniplc0java\\out.o0";
        try {
            input = new FileInputStream(inputFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        FileOutputStream output;
        try {
            // output = new PrintStream(new FileOutputStream(outputFileName));
            output = new FileOutputStream(outputFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open output file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        var analyzer = new Analyser(tokenizer);
        analyzer.analyse();
        OutPutBinary answer = new OutPutBinary(analyzer.currentTable, analyzer.funcTable);
        List<Byte> bytes = answer.generate();
        System.out.println(bytes);
        for (Byte b : bytes)
            output.write(b);
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }

}
