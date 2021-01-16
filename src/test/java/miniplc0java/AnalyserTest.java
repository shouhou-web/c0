package miniplc0java;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.SymbolTable;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.vm.Output;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;

public class AnalyserTest {


    @Test
    public void test() {
        InputStream input;
        String inputFileName = "D:\\北京航空航天大学\\大三学习资料\\编译原理\\c0\\src\\main\\java\\miniplc0java\\in.txt";
        String outputFileName = "D:\\北京航空航天大学\\大三学习资料\\编译原理\\c0\\src\\main\\java\\miniplc0java\\out.txt";
        try {
            input = new FileInputStream(inputFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        PrintStream output;
        try {
            output = new PrintStream(new FileOutputStream(outputFileName));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open output file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        try {
            Scanner scanner;
            scanner = new Scanner(input);
            var iter = new StringIter(scanner);
            var tokenizer = tokenize(iter);

            var analyzer = new Analyser(tokenizer);
            analyzer.analyse();
            Output answer= new Output(analyzer.currentTable,analyzer.funcTable);

//            System.out.println(answer.toVmCode());
            output.print(answer.toVmCode());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }

}
