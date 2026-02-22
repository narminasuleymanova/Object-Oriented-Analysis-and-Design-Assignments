package assignment2;

public class Writer {
    private final RingBuffer rb;

    public Writer(RingBuffer rb) {
        this.rb = rb;
    }

    public void write(int x) {
        rb.writeInternal(x);
        System.out.println("Writer wrote: " + x);
    }

}