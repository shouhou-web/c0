package miniplc0java;

import miniplc0java.analyser.*;
import miniplc0java.error.CompileError;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.vm.OutPutBinary;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class App {
    public static void main(String[] args) throws Exception {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("asm");
        System.out.println(inputFileName + " " + outputFileName);

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
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

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("asm")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }

    private static ArrayList<Byte> handleInt(int num) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(0, num);
        ArrayList<Byte> res = new ArrayList<>();
        for (byte b : buffer.array()) {
            res.add(b);
        }
        return res;
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}