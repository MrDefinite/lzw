package org.caroline.lzw.decompression;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.caroline.lzw.util.ByteArray;
import org.caroline.lzw.util.FileCharReader;

public class SimpleDecompression {

    private static final int FILE_END_CODE = 4095;
    private static final int CLEAR_CODE = 256;
    private static final int CODE_WIDE = 12;

    private FileCharReader fileCharReader;
    private String inputFileName;
    private int skipNum;
    private String[] outputFileNames;
    private List<ByteArray> table;
    private int currentFileCount = 0;
    private OutputStream bufferedOut;
    private boolean writerFormerFile = false;
    private String save;

    int oldCode;
    int character;
    int newCode;

    public SimpleDecompression(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public void doDecompressionProcess() {
        parseFileNames();
        skipNum = getSkipCount();
        fileCharReader = new FileCharReader(inputFileName, skipNum);

        System.out.println("Decompressing file: "
                + outputFileNames[currentFileCount] + "!");
        try {
            bufferedOut = new BufferedOutputStream(new FileOutputStream(
                    outputFileNames[currentFileCount]));

            System.out.println("Init Enrty table!");
            initTable();

            oldCode = readCodeFromFile();
            bufferedOut.write(oldCode);
            character = oldCode;
            while ((newCode = readCodeFromFile()) != -1) {
                if (newCode == FILE_END_CODE) {
                    currentFileCount++;
                    if (currentFileCount < outputFileNames.length) {
                        writerFormerFile = true;
                        save = outputFileNames[currentFileCount];

                    } else {
                        bufferedOut.flush();
                        bufferedOut.close();
                        return;
                    }
                    continue;

                } else if (newCode == CLEAR_CODE) {
                    initTable();
                    oldCode = readCodeFromFile();
                    bufferedOut.write(oldCode);
                    character = oldCode;
                } else {
                    ByteArray string;
                    if (newCode >= table.size()) {
                        string = new ByteArray(table.get(oldCode));
                        string.append((byte) character);
                    } else {
                        string = table.get(newCode);
                    }

                    System.out.println("Writing code: '"
                            + new String(string.internal) + "'.");

                    for (int i = 0; i < string.size(); i++) {
                        try {
                            bufferedOut.write(string.get(i));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (writerFormerFile) {
                        bufferedOut.flush();
                        bufferedOut.close();

                        System.out.println("Writing file: " + save + "!");
                        bufferedOut = new BufferedOutputStream(
                                new FileOutputStream(save));

                        writerFormerFile = false;
                    }

                    character = string.get(0);
                    table.add(new ByteArray(table.get(oldCode))
                            .append((byte) character));
                    oldCode = newCode;

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeBufferToFile(OutputStream bufferedOut) {
        ByteArray string;
        if (newCode >= table.size()) {
            string = new ByteArray(table.get(oldCode));
            string.append((byte) character);
        } else {
            string = table.get(newCode);
        }
        for (int i = 0; i < string.size(); i++) {
            try {
                bufferedOut.write(string.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        character = string.get(0);
        table.add(new ByteArray(table.get(oldCode)).append((byte) character));
        oldCode = newCode;
    }

    private void initTable() {
        table = new ArrayList<ByteArray>();
        for (int i = 0; i < 257; i++) {
            table.add(new ByteArray((byte) i));
        }
    }

    private void parseFileNames() {
        System.out.println("Begin to parsed file names!");

        List<String> tmpFileNames = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(inputFileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    System.out
                            .println("Parsed file name finished, begin to decompress file.");
                    break;
                } else {
                    System.out.println("Parsed file name: " + line + ".");
                    tmpFileNames.add(line);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outputFileNames = new String[tmpFileNames.size()];
        tmpFileNames.toArray(outputFileNames);
    }

    private int readCodeFromFile() {
        int code = fileCharReader.readInt(CODE_WIDE);
        System.out.println("Reading code is: " + code + "!");
        return code;
    }

    private int getSkipCount() {
        String skipCountString = "";
        for (int i = 0; i < outputFileNames.length; i++) {
            skipCountString += outputFileNames[i] + "\n";
        }
        skipCountString += "\n";
        try {
            byte[] utf8Bytes = skipCountString.getBytes("UTF-8");
            System.out.println("Skip number is: " + utf8Bytes.length + ".");

            return utf8Bytes.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
