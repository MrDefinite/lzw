package org.caroline.lzw.decompression;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.caroline.lzw.util.FileCharReader;
import org.caroline.lzw.util.FileCharWriter;
import org.caroline.lzw.util.Table;

public class Decompression {

    private FileCharWriter fileCharWriter;
    private FileCharReader fileCharReader;
    private int skipNum;
    private String[] outputFileNames;
    private DecompressionTable table;
    private int currentCode;
    private int prevCode;
    private String inputFileName;
    private int currentFileCount = 0;
    private final int bufferLimit = Table.TABLE_SIZE;
    private char[] buffer = new char[bufferLimit];
    private int bufferCount = 0;
    private int codeCount = resetCodeCount();

    public Decompression(String inputFileName) {
        table = new DecompressionTable();
        this.inputFileName = inputFileName;
    }

    public void doDecompressionProcess() {
        parseFileNames();
        skipNum = getSkipCount();
        fileCharReader = new FileCharReader(inputFileName, skipNum);

        System.out.println("Writing file: " + outputFileNames[currentFileCount]
                + "!");
        fileCharWriter = new FileCharWriter(outputFileNames[currentFileCount]);

        while ((prevCode = readCodeFromFile()) != Table.CLEAR_CODE) {
            if (prevCode == -1) {
                return;
            }
        }

        System.out.println("Init Enrty table!");
        table.initTable();

        prevCode = readCodeFromFile();
        buffer[bufferCount] = (char) prevCode;
        bufferCount++;

        while ((currentCode = readCodeFromFile()) != -1) {
            if (currentCode == Table.FILE_END_CODE) {

                writeBufferToFile();

                fileCharWriter.flush();
                fileCharWriter.close();

                currentFileCount++;

                if (currentFileCount < outputFileNames.length) {
                    System.out.println("Writing file: "
                            + outputFileNames[currentFileCount] + "!");
                    fileCharWriter = new FileCharWriter(
                            outputFileNames[currentFileCount]);
                } else {
                    return;
                }
                continue;
            }

            if (currentCode == Table.CLEAR_CODE) {
                resetCodeCount();
                table.initTable();

                prevCode = readCodeFromFile();

                continue;
            }

            if (currentCode > table.currentPos) {
                throw new IndexOutOfBoundsException(
                        "localCode larger than currentPos!");
            }

            if (table.stringTable[currentCode].isUsed()) {

            } else {

            }

            if (codeCount != 0) {
                table.updateTable(prevCode, (char) currentCode);
                codeCount--;
            }
            prevCode = currentCode;

            if (bufferCount < bufferLimit) {
                buffer[bufferCount] = (char) currentCode;
                bufferCount++;
            } else {
                writeBufferToFile();
            }

        }

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

    private int resetCodeCount() {
        codeCount = Table.FILE_END_CODE - Table.BEGIN_CODE;
        System.out.println("Code count is set back to: " + codeCount + "!");
        return codeCount;
    }

    private int readCodeFromFile() {
        int code = fileCharReader.readInt(Table.CODE_WIDE);
        System.out.println("Reading code is: " + code + "!");
        return code;
    }

    private void writeCharToFile(char[] x) {
        String res = new String(x);
        System.out.println("Writing char " + res + " into file!");
        fileCharWriter.write(res);
    }

    private void writeBufferToFile() {
        writeCharToFile(buffer);
        bufferCount = 0;
        buffer = new char[Table.TABLE_SIZE];
    }

    private char[] getChars(int begin, int end) {
        char[] res = new char[end - begin + 1];

        for (int i = begin; i <= end; i++) {
            res[i - begin] = table.stringTable[i].getCharacter();
        }

        return res;
    }
}
