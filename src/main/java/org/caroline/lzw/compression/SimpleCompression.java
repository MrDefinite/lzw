package org.caroline.lzw.compression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.caroline.lzw.util.ByteArray;
import org.caroline.lzw.util.FileCharWriter;

public class SimpleCompression {
    private static final Logger logger = LogManager
            .getLogger(SimpleCompression.class);
    private static final int FILE_END_CODE = 4095;
    private static final int CLEAR_CODE = 256;
    private static final int BEGIN_CODE = 257;
    private static final int CODE_WIDE = 12;
    private static int MAX_CODE = (1 << CODE_WIDE) - 1;

    private FileCharWriter fileCharWriter;
    private String[] inputFileNames;
    private Map<ByteArray, Integer> table;
    private int curCode;
    private ByteArray w;

    public SimpleCompression(String outputFilename, String[] inputFileNames) {
        logger.info("Compression module init!");

        logger.info("Init file char writer!");
        fileCharWriter = new FileCharWriter(outputFilename);
        this.inputFileNames = inputFileNames;
    }

    public void doCompressionProcess() {
        logger.info("Do compression process!");

        writeFileHead();

        for (int i = 0; i < inputFileNames.length; i++) {
            compressSingleFile(inputFileNames[i], i);
        }

        logger.info("Closing file char writer!");
        fileCharWriter.flush();
        fileCharWriter.close();

        logger.info("Compression finished!");
    }

    private void compressSingleFile(String fileName, int loc) {
        logger.info("Compressing file: '" + fileName + "'!");

        try {
            InputStream bufferedIn = new BufferedInputStream(
                    new FileInputStream(fileName));

            if (loc == 0) {
                initTable();

                int firstByte = bufferedIn.read();
                w = new ByteArray((byte) firstByte);
            }

            int curByte;
            while ((curByte = bufferedIn.read()) != -1) {
                ByteArray wK = new ByteArray(w).append((byte) curByte);
                if (table.containsKey(wK)) {
                    w = wK;
                } else {
                    writeCharToFile(table.get(w));
                    if (curCode < MAX_CODE) {
                        table.put(wK, curCode);
                        curCode++;
                    } else {
                        writeCharToFile(CLEAR_CODE);
                        initTable();
                    }
                    w = new ByteArray((byte) curByte);
                }
            }

            if (loc == inputFileNames.length - 1) {
                writeCharToFile(table.get(w));
            }

            bufferedIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeFileEndCode(fileName);
    }

    private void writeFileEndCode(String fileName) {
        logger.info("Compress file: '" + fileName
                + "' finished! Write file end mark!");
        writeCharToFile(FILE_END_CODE);
    }

    private void writeFileHead() {
        logger.info("Writing head info!");

        for (int i = 0; i < inputFileNames.length; i++) {
            logger.info("Write file name: '" + inputFileNames[i]
                    + "' into output file!");
            fileCharWriter.write(inputFileNames[i] + "\n");
        }

        logger.info("File names writing finished, write return code into output file!");
        fileCharWriter.write("\n");
    }

    private void writeCharToFile(int x) {
        logger.debug("Writing encoded char " + x + " into file!");
        fileCharWriter.write(x, CODE_WIDE);
    }

    private void initTable() {
        logger.info("Init enrty table!");

        curCode = BEGIN_CODE;
        table = new HashMap<ByteArray, Integer>();
        for (int i = 0; i < BEGIN_CODE; i++) {
            table.put(new ByteArray((byte) i), i);
        }

        logger.info("Init enrty table finish!");
    }
}
