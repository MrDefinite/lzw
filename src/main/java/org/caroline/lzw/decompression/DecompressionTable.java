package org.caroline.lzw.decompression;

import org.caroline.lzw.util.Table;

public class DecompressionTable extends Table {

    public DecompressionTable() {
    }

    @Override
    public int updateTable(int prevcode, char c) {
        if (currentPos >= TABLE_SIZE - 1) {
            return NOT_FIND;
        }

        stringTable[currentPos].setUsed(true);
        stringTable[currentPos].setPrev(prevcode);
        stringTable[currentPos].setCharacter(c);
        currentPos++;

        return currentPos;
    }

}
