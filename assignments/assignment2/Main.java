package assignment2;

class Main {
    public static void main(String[] args) {
        RingBuffer rb = new RingBuffer(4);
        Writer writer = new Writer(rb);

        // two independent readers
        Reader r1 = new Reader(rb);
        Reader r2 = new Reader(rb);

        rb.printState("Initial", r1, r2);

        // write some items
        writer.write(10);
        writer.write(20);
        writer.write(30);
        rb.printState("After writing 10,20,30", r1, r2);

        // readers read independently
        r1.readAndPrint("Reader1"); // 10
        r2.readAndPrint("Reader2"); // 10
        r2.readAndPrint("Reader2"); // 20
        rb.printState("After some reads", r1, r2);

        // write more (causes overwrite when capacity exceeded)
        writer.write(40);
        writer.write(50); // overwrites oldest
        rb.printState("After writing 40,50 (overwrite happens)", r1, r2);

        // read again
        r1.readAndPrint("Reader1");
        r1.readAndPrint("Reader1");
        r1.readAndPrint("Reader1");
        r2.readAndPrint("Reader2");
        r2.readAndPrint("Reader2");
        rb.printState("Final", r1, r2);
    }
}
