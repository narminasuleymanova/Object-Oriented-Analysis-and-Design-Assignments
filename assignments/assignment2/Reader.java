package assignment2;

public class Reader {
    final RingBuffer rb;
    long nextSeq; // this reader's independent position

    // starts from OLDEST available
    public Reader(RingBuffer rb) {
        this.rb = rb;
        long writeSeq = rb.currentWriteSeq();
        int n = rb.capacity();
        this.nextSeq = Math.max(0, writeSeq - n);
    }

    public void readAndPrint(String readerName) {
        Integer v = rb.readInternal(this);
        if (v == null) {
            System.out.println(readerName + " read: nothing new");
        } else {
            System.out.println(readerName + " read this message: " + v);
        }
    }
}