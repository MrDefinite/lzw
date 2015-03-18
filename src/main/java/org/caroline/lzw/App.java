package org.caroline.lzw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.caroline.lzw.compression.SimpleCompression;
import org.caroline.lzw.decompression.SimpleDecompression;

/**
 * LZW algorithm
 * 
 * @author Caroline
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        if (args == null || args.length <= 1) {
            logger.error("You need input more args!");
            return;
        }

        String cmd = args[0];

        if (cmd.equals("-c") || cmd.equals("-C")) {
            if (args.length <= 2) {
                logger.error("You need input more args!");
                return;
            }
            logger.info("Compression process running!");

            String outputFileName = args[1];
            String[] inputFileNames = new String[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                inputFileNames[i - 2] = args[i];
            }

            compression.doCompressionProcess();

        } else if (cmd.equals("-d") || cmd.equals("-D")) {
            if (args.length > 2) {
                System.out.println("Your args is too much!");
                return;
            }

            String inputFileName = args[1];

            decompression.doDecompressionProcess();

        } else {
        }

    }
}
