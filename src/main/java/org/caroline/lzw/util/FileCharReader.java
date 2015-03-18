package org.caroline.lzw.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileCharReader {

    private BufferedInputStream in;
    private final int EOF = -1; // end of file

    private int buffer; // one character buffer
    private int N; // number of bits left in buffer

    public FileCharReader(String inputFileName) {
        try {
            in = new BufferedInputStream(new FileInputStream(inputFileName));
            fillBuffer();
        } catch (FileNotFoundException e) {
            buffer = EOF;
            N = -1;
            e.printStackTrace();
        }
    }

    public FileCharReader(String inputFileName, long skipNum) {
        try {
            in = new BufferedInputStream(new FileInputStream(inputFileName));
            in.skip(skipNum);
            fillBuffer();
        } catch (FileNotFoundException e) {
            buffer = EOF;
            N = -1;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillBuffer() {
        try {
            buffer = in.read();
            N = 8;
        } catch (IOException e) {
            System.out.println("EOF");
            buffer = EOF;
            N = -1;
        }
    }

    /**
     * Close this input stream and release any associated system resources.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not close BinaryStdIn");
        }
    }

    /**
     * Returns true if standard input is empty.
     * 
     * @return true if and only if standard input is empty
     */
    public boolean isEmpty() {
        return buffer == EOF;
    }

    /**
     * Read the next bit of data from standard input and return as a boolean.
     * 
     * @return the next bit of data from standard input as a <tt>boolean</tt>
     * @throws RuntimeException
     *             if standard input is empty
     */
    public boolean readBoolean() {
        if (isEmpty())
            throw new RuntimeException("Reading from empty input stream");
        N--;
        boolean bit = ((buffer >> N) & 1) == 1;
        if (N == 0)
            fillBuffer();
        return bit;
    }

    /**
     * Read the next 8 bits from standard input and return as an 8-bit char.
     * Note that <tt>char</tt> is a 16-bit type; to read the next 16 bits as a
     * char, use <tt>readChar(16)</tt>
     * 
     * @return the next 8 bits of data from standard input as a <tt>char</tt>
     * @throws RuntimeException
     *             if there are fewer than 8 bits available on standard input
     */
    public char readChar() {
        if (isEmpty())
            throw new RuntimeException("Reading from empty input stream");

        // special case when aligned byte
        if (N == 8) {
            int x = buffer;
            fillBuffer();
            return (char) (x & 0xff);
        }

        // combine last N bits of current buffer with first 8-N bits of new
        // buffer
        int x = buffer;
        x <<= (8 - N);
        int oldN = N;
        fillBuffer();
        if (isEmpty())
            throw new RuntimeException("Reading from empty input stream");
        N = oldN;
        x |= (buffer >>> N);
        return (char) (x & 0xff);
        // the above code doesn't quite work for the last character if N = 8
        // because buffer will be -1
    }

    /**
     * Read the next r bits from standard input and return as an r-bit
     * character.
     * 
     * @param r
     *            number of bits to read.
     * @return the next r bits of data from standard input as a <tt>char</tt>
     * @throws IllegalArgumentException
     *             if there are fewer than r bits available on standard input
     * @throws IllegalArgumentException
     *             unless 1 &le; r &le; 16
     */
    public char readChar(int r) {
        if (r < 1 || r > 16)
            throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 8 case
        if (r == 8)
            return readChar();

        char x = 0;
        for (int i = 0; i < r; i++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit)
                x |= 1;
        }
        return x;
    }

    /**
     * Read the remaining bytes of data from standard input and return as a
     * string.
     * 
     * @return the remaining bytes of data from standard input as a
     *         <tt>String</tt>
     * @throws RuntimeException
     *             if standard input is empty or if the number of bits available
     *             on standard input is not a multiple of 8 (byte-aligned)
     */
    public String readString() {
        if (isEmpty())
            throw new RuntimeException("Reading from empty input stream");

        StringBuilder sb = new StringBuilder();
        while (!isEmpty()) {
            char c = readChar();
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Read the next 16 bits from standard input and return as a 16-bit short.
     * 
     * @return the next 16 bits of data from standard input as a <tt>short</tt>
     * @throws RuntimeException
     *             if there are fewer than 16 bits available on standard input
     */
    public short readShort() {
        short x = 0;
        for (int i = 0; i < 2; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

    /**
     * Read the next 32 bits from standard input and return as a 32-bit int.
     * 
     * @return the next 32 bits of data from standard input as a <tt>int</tt>
     * @throws RuntimeException
     *             if there are fewer than 32 bits available on standard input
     */
    public int readInt() {
        int x = 0;
        for (int i = 0; i < 4; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

    /**
     * Read the next r bits from standard input and return as an r-bit int.
     * 
     * @param r
     *            number of bits to read.
     * @return the next r bits of data from standard input as a <tt>int</tt>
     * @throws IllegalArgumentException
     *             if there are fewer than r bits available on standard input
     * @throws IllegalArgumentException
     *             unless 1 &le; r &le; 32
     */
    public int readInt(int r) {
        if (r < 1 || r > 32)
            throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 32 case
        if (r == 32)
            return readInt();

        int x = 0;
        for (int i = 0; i < r; i++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit)
                x |= 1;
        }
        return x;
    }

    /**
     * Read the next 64 bits from standard input and return as a 64-bit long.
     * 
     * @return the next 64 bits of data from standard input as a <tt>long</tt>
     * @throws RuntimeException
     *             if there are fewer than 64 bits available on standard input
     */
    public long readLong() {
        long x = 0;
        for (int i = 0; i < 8; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

    /**
     * Read the next 64 bits from standard input and return as a 64-bit double.
     * 
     * @return the next 64 bits of data from standard input as a <tt>double</tt>
     * @throws RuntimeExceptionArgument
     *             if there are fewer than 64 bits available on standard input
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read the next 32 bits from standard input and return as a 32-bit float.
     * 
     * @return the next 32 bits of data from standard input as a <tt>float</tt>
     * @throws RuntimeException
     *             if there are fewer than 32 bits available on standard input
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Read the next 8 bits from standard input and return as an 8-bit byte.
     * 
     * @return the next 8 bits of data from standard input as a <tt>byte</tt>
     * @throws RuntimeException
     *             if there are fewer than 8 bits available on standard input
     */
    public byte readByte() {
        char c = readChar();
        byte x = (byte) (c & 0xff);
        return x;
    }

}
