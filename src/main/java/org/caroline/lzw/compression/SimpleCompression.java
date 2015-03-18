package org.caroline.lzw.compression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.caroline.lzw.util.ByteArray;
import org.caroline.lzw.util.FileCharWriter;
import org.caroline.lzw.util.Table;

public class SimpleCompression {
    private FileCharWriter fileCharWriter;
    private String[] inputFileNames;
    private Map<ByteArray, Integer> table;
    // private static final int TABLE_SIZE = 4096;
    private static final int FILE_END_CODE = 4095;
    private static final int CLEAR_CODE = 256;
    private static final int BEGIN_CODE = 257;
    private static final int CODE_WIDE = 12;
    private static int MAX_CODE = (1 << CODE_WIDE) - 1;
    private int curCode;
    private ByteArray w;
    private boolean isFirstRead = true;

    public SimpleCompression(String outputFilename, String[] inputFileNames) {

        fileCharWriter = new FileCharWriter(outputFilename);
        this.inputFileNames = inputFileNames;
    }

    public void doCompressionProcess() {
        System.out.println("Writing head info!");
        writeFileHead();

        for (int i = 0; i < inputFileNames.length; i++) {
            System.out.println("Compress file: " + inputFileNames[i] + "!");

            compressSingleFile(inputFileNames[i]);

            if (i == inputFileNames.length - 1) {
                writeCharToFile(table.get(w));
            }

            System.out.println("Compress file: " + inputFileNames[i]
                    + " finished! Write file end mark!");
            writeCharToFile(FILE_END_CODE);
        }

        System.out.println("Compression finished!");
        fileCharWriter.flush();
        fileCharWriter.close();
    }

    private void compressSingleFile(String fileName) {
        try {
            InputStream bufferedIn = new BufferedInputStream(
                    new FileInputStream(fileName));

            if (isFirstRead) {
                System.out.println("Init Enrty table!");
                initTable();

                int firstByte = bufferedIn.read();
                w = new ByteArray((byte) firstByte);

                isFirstRead = false;
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
            // writeCharToFile(table.get(w));

            // System.out.println("Compress file: " + fileName
            // + " finished! Write file end mark!");
            // writeCharToFile(FILE_END_CODE);

            bufferedIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileHead() {
        for (int i = 0; i < inputFileNames.length; i++) {
            System.out.println("Write file name: " + inputFileNames[i]
                    + " into output file!");
            fileCharWriter.write(inputFileNames[i] + "\n");
        }

        System.out
                .println("File names writing finished, write return code into output file!");
        fileCharWriter.write("\n");
    }

    private void writeCharToFile(int x) {
        System.out.println("Writing encoded char " + x + " into file!");
        fileCharWriter.write(x, Table.CODE_WIDE);
    }

    private void initTable() {
        curCode = BEGIN_CODE;
        table = new HashMap<ByteArray, Integer>();
        for (int i = 0; i < 257; i++) {
            table.put(new ByteArray((byte) i), i);
        }
    }
}
