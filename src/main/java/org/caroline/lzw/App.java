package org.caroline.lzw;

import org.caroline.lzw.compression.Compression;
import org.caroline.lzw.compression.SimpleCompression;
import org.caroline.lzw.decompression.Decompression;
import org.caroline.lzw.decompression.SimpleDecompression;

/**
 * LZW
 *
 */
public class App {
    public static void main(String[] args) {
        if (args == null || args.length <= 1) {
            System.out.println("You need input more args!");
            return;
        }

        String cmd = args[0];

        if (cmd.equals("-c") || cmd.equals("-C")) {
            if (args.length <= 2) {
                System.out.println("You need input more args!");
                return;
            }
            System.out.println("Compression process running!");

            String outputFileName = args[1];
            String[] inputFileNames = new String[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                inputFileNames[i - 2] = args[i];
            }

            SimpleCompression compression = new SimpleCompression(outputFileName,
                    inputFileNames);
            compression.doCompressionProcess();

        } else if (cmd.equals("-d") || cmd.equals("-D")) {
            if (args.length > 2) {
                System.out.println("Your args is too much!");
                return;
            }
            System.out.println("Decompression process running!");

            String inputFileName = args[1];

            SimpleDecompression decompression = new SimpleDecompression(inputFileName);
            decompression.doDecompressionProcess();

        } else {
            System.out.println("You input option" + cmd + " cannot be parsed!");
        }

    }
}
