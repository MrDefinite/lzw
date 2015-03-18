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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.caroline.lzw.util.ByteArray;
import org.caroline.lzw.util.FileCharReader;

public class SimpleDecompression {
    private static final Logger logger = LogManager
            .getLogger(SimpleDecompression.class);
    private static final int FILE_END_CODE = 4095;
    private static final int CLEAR_CODE = 256;
    private static final int BEGIN_CODE = 257;
    private static final int CODE_WIDE = 12;

    private FileCharReader fileCharReader;
    private String inputFileName;
    private int skipNum;
    private String[] outputFileNames;
    private List<ByteArray> table;
    private int currentFileCount = 0;
    private OutputStream bufferedOut;
    private boolean handleFormerFile = false;

    int oldCode;
    int character;
    int newCode;

    public SimpleDecompression(String inputFileName) {
        logger.info("Decompression module init!");
        this.inputFileName = inputFileName;
    }

    public void doDecompressionProcess() {
        logger.info("Do decompression process!");

        parseFileNames();
        skipNum = getSkipCount();

        if (skipNum < 0) {
            logger.error("Cannot find skip number!");
            return;
        }
        fileCharReader = new FileCharReader(inputFileName, skipNum);

        logger.info("Decompressing file: " + outputFileNames[currentFileCount]
                + "!");
        try {
            initBufferOutputStream(outputFileNames[currentFileCount]);

            initTable();
            readFileFirstTime();

            while ((newCode = readCodeFromFile()) != -1) {
                if (newCode == FILE_END_CODE) {
                    handleFileEndCode();
                    continue;

                } else if (newCode == CLEAR_CODE) {
                    initTable();
                    readFileFirstTime();
                } else {
                    ByteArray string;
                    if (newCode >= table.size()) {
                        string = new ByteArray(table.get(oldCode));
                        string.append((byte) character);
                    } else {
                        string = table.get(newCode);
                    }

                    writeStringIntoFile(string);

                    if (handleFormerFile) {
                        bufferedOut.flush();
                        bufferedOut.close();

                        initBufferOutputStream(outputFileNames[currentFileCount]);

                        handleFormerFile = false;
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

        logger.info("Decompression finished!");
    }

    private void initBufferOutputStream(String fileName) {
        logger.info("Writing file: '" + fileName + "'!");
        try {
            bufferedOut = new BufferedOutputStream(new FileOutputStream(
                    fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeStringIntoFile(ByteArray byteArray) {
        logger.debug("Writing code: '" + new String(byteArray.getArray())
                + "'.");

        for (int i = 0; i < byteArray.size(); i++) {
            try {
                bufferedOut.write(byteArray.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileEndCode() {
        currentFileCount++;
        if (currentFileCount < outputFileNames.length) {
            handleFormerFile = true;
        } else {
            try {
                bufferedOut.flush();
                bufferedOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void readFileFirstTime() {
        try {
            oldCode = readCodeFromFile();
            bufferedOut.write(oldCode);
            character = oldCode;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTable() {
        logger.info("Init Enrty table!");

        table = new ArrayList<ByteArray>();
        for (int i = 0; i < BEGIN_CODE; i++) {
            table.add(new ByteArray((byte) i));
        }

        logger.info("Init enrty table finish!");
    }

    private void parseFileNames() {
        logger.info("Begin to parsed file names!");

        List<String> tmpFileNames = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(inputFileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    logger.info("Parsed file name finished, begin to decompress file.");
                    break;
                } else {
                    logger.info("The file name is: '" + line + "'.");
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
        logger.debug("Reading code is: " + code + "!");
        return code;
    }

    private int getSkipCount() {
        StringBuffer skipCountString = new StringBuffer();
        for (int i = 0; i < outputFileNames.length; i++) {
            skipCountString.append(outputFileNames[i] + "\n");
        }
        skipCountString.append("\n");
        try {
            byte[] utf8Bytes = skipCountString.toString().getBytes("UTF-8");
            logger.info("Skip number is: " + utf8Bytes.length + ".");

            return utf8Bytes.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
