package org.caroline.lzw.compression;

import org.caroline.lzw.util.Table;

public class CompressionTable extends Table {

    public CompressionTable() {
    }

    @Override
    public int updateTable(int prevcode, char c) {
        if (currentPos >= TABLE_SIZE - 1 || prevcode >= TABLE_SIZE - 2) {
            return NOT_FIND;
        }

        stringTable[currentPos].setUsed(true);
        stringTable[currentPos].setPrev(prevcode);
        stringTable[currentPos].setCharacter(c);
        currentPos++;

        return currentPos;
    }

}
