package org.caroline.lzw.util;

public abstract class Table {

    public static final int TABLE_SIZE = 4096;
    public static final int FILE_END_CODE = 4095;
    public static final int CLEAR_CODE = 256;
    public static final int BEGIN_CODE = 257;
    public static final int CODE_WIDE = 12;
    public static final int NOT_FIND = -1;
    public static final int NO_PREV = -1;
    public static final int NO_NEXT = -1;
    public Entry[] stringTable;
    protected int nextCode;
    public int currentPos;

    public Table() {
        stringTable = new Entry[TABLE_SIZE];
    }

    public void initTable() {
        currentPos = BEGIN_CODE;
        for (int i = 0; i < TABLE_SIZE; i++) {
            stringTable[i] = new Entry();
            stringTable[i].setUsed(false);
            stringTable[i].setPrev(-1);
            stringTable[i].setCharacter((char) 0);
        }

        for (int i = 0; i < BEGIN_CODE; i++) {
            stringTable[i].setUsed(true);
            stringTable[i].setCharacter((char) i);
        }
        stringTable[TABLE_SIZE - 1].setUsed(true);
        stringTable[TABLE_SIZE - 1].setCharacter((char) (TABLE_SIZE - 1));
    }

    public abstract int updateTable(int prevcode, char c);

    public int queryTable(int code, char c) {
        Entry entry;

        if (code == NO_PREV) {
            return c;
        }

        for (int i = code + 1; i < currentPos; i++) {
            entry = stringTable[i];
            if (entry.isUsed() == true && entry.getPrev() == code
                    && entry.getCharacter() == c) {
                return i;
            }
        }

        return NOT_FIND;
    }

}
