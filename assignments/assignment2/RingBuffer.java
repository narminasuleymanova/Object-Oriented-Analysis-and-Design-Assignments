package assignment2;

import java.util.ArrayList;
import java.util.List;

public class RingBuffer {
    private final List<Integer> buffer;
    private final int n;

    private int w = 0;
    private long writeSeq = 0;

    public RingBuffer(int n) {
        if (n <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.n = n;
        this.buffer = new ArrayList<>(n);
        for (int i = 0; i < n; i++) buffer.add(0);
    }

    void writeInternal(int x) {
        buffer.set(w, x);
        w++;
        if (w == n) {
            w = 0;
        }
        writeSeq++;
    }

    Integer readInternal(Reader reader) {
        long oldest = Math.max(0, writeSeq - n);

        // too slow -> missed overwritten items -> jump forward
        if (reader.nextSeq < oldest) reader.nextSeq = oldest;

        // nothing new for this reader
        if (reader.nextSeq >= writeSeq) return null;

        int idx = (int) (reader.nextSeq % n);
        int value = buffer.get(idx);
        reader.nextSeq++;
        return value;
    }

    long currentWriteSeq() { return writeSeq; }
    int capacity() { return n; }

    public void printState(String title, Reader... readers) {
        System.out.println("\n=== " + title + " ===");
        System.out.println("buffer: " + buffer);
        System.out.println("writeIndex (w): " + w);
        System.out.println("writeSeq: " + writeSeq);
        System.out.println("oldestAvailableSeq: " + Math.max(0, writeSeq - n));
        for (int i = 0; i < readers.length; i++) {
            System.out.println("reader" + (i + 1) + " nextSeq: " + readers[i].nextSeq);
        }
    }
}