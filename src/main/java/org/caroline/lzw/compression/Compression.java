package org.caroline.lzw.compression;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.caroline.lzw.util.FileCharWriter;
import org.caroline.lzw.util.Table;

public class Compression {

    private FileCharWriter fileCharWriter;
    private String[] inputFileNames;
    private CompressionTable table;
    private int currentCode;
    private int localCode;
    private int prevCode;
    private boolean isFirstRead = true;
    private int codeCount = resetCodeCount();

    public Compression(String outputFilename, String[] inputFileNames) {
        fileCharWriter = new FileCharWriter(outputFilename);
        this.inputFileNames = inputFileNames;

        table = new CompressionTable();
    }

    public void doCompressionProcess() {
        writeFileHead();

        System.out.println("Init Enrty table!");
        table.initTable();

        System.out.println("Writing CLEAR CODE!");
        writeCharToFile(Table.CLEAR_CODE);

        for (int i = 0; i < inputFileNames.length; i++) {
            System.out.println("Compress file: " + inputFileNames[i] + "!");
            compressSingleFile(inputFileNames[i]);
        }

        System.out.println("Compression finished!");

        fileCharWriter.flush();
        fileCharWriter.close();
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

    private void compressSingleFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)));

            if (isFirstRead == true) {
                currentCode = reader.read();
                if (currentCode == -1) {
                    reader.close();
                    return;
                }

                prevCode = table.queryTable(Table.NO_PREV, (char) currentCode);
                isFirstRead = false;
            }

            while ((currentCode = reader.read()) != -1) {

                if ((localCode = table.queryTable(prevCode, (char) currentCode)) != Table.NOT_FIND) {
                    prevCode = localCode;
                    continue;
                }

                writeCharToFile(prevCode);

                if (codeCount != 0) {
                    table.updateTable(prevCode, (char) currentCode);
                    codeCount--;
                }

                if (codeCount == 0) {
                    resetCodeCount();

                    System.out.println("Dict is full, Reinit Enrty table!");
                    table.initTable();

                    System.out.println("Writing CLEAR CODE!");
                    writeCharToFile(Table.CLEAR_CODE);
                }
                prevCode = table.queryTable(Table.NO_PREV, (char) currentCode);
            }

            writeCharToFile(prevCode);

            System.out.println("Compress file: " + fileName
                    + " finished! Write file end mark!");
            writeCharToFile(Table.FILE_END_CODE);

            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int resetCodeCount() {
        codeCount = Table.FILE_END_CODE - Table.BEGIN_CODE;
        System.out.println("Code count is set back to: " + codeCount + "!");
        return codeCount;
    }

    private void writeCharToFile(int x) {
        System.out.println("Writing char " + (char) x + " into file!");
        fileCharWriter.write(x, Table.CODE_WIDE);
    }

}
