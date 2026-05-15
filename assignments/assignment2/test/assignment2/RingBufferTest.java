package assignment2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Ring Buffer project.
 *
 * Classes under test: RingBuffer, Reader, Writer
 *
 * NOTE: Some internal state (e.g., buffer contents, write index 'w') is not
 * directly accessible because the fields are private and no getters exist.
 * Those aspects are tested indirectly through Reader behavior.
 * This is documented per requirement ("document in PR instead of changing the code").
 *
 * Testability note:
 *   - RingBuffer.writeInternal() and RingBuffer.readInternal() are package-private,
 *     so they are accessible within the same package (assignment2).
 *   - RingBuffer.buffer, RingBuffer.w are private with no getters — tested indirectly.
 *   - Writer.write() prints to stdout; output side-effects are not asserted here
 *     (would require capturing System.out), but the functional behavior is verified
 *     through subsequent reads.
 */
class RingBufferTest {

    // -------------------------------------------------------------------------
    // RingBuffer — Constructor
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("RingBuffer: capacity() returns the value passed to constructor")
    void testCapacityMatchesConstructorArg() {
        RingBuffer rb = new RingBuffer(4);
        assertEquals(4, rb.capacity());
    }

    @Test
    @DisplayName("RingBuffer: initial writeSeq is 0")
    void testInitialWriteSeqIsZero() {
        RingBuffer rb = new RingBuffer(4);
        assertEquals(0L, rb.currentWriteSeq());
    }

    @Test
    @DisplayName("RingBuffer: capacity of 1 is valid")
    void testCapacityOne() {
        RingBuffer rb = new RingBuffer(1);
        assertEquals(1, rb.capacity());
    }

    @Test
    @DisplayName("RingBuffer: capacity of 0 throws IllegalArgumentException")
    void testCapacityZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer(0));
    }

    @Test
    @DisplayName("RingBuffer: negative capacity throws IllegalArgumentException")
    void testNegativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer(-5));
    }

    // -------------------------------------------------------------------------
    // RingBuffer — writeInternal / currentWriteSeq
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("RingBuffer: writeSeq increments by 1 after each write")
    void testWriteSeqIncrementsOnEachWrite() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(10);
        assertEquals(1L, rb.currentWriteSeq());
        rb.writeInternal(20);
        assertEquals(2L, rb.currentWriteSeq());
        rb.writeInternal(30);
        assertEquals(3L, rb.currentWriteSeq());
    }

    @Test
    @DisplayName("RingBuffer: writeSeq increments past capacity (no wrap)")
    void testWriteSeqDoesNotWrapAtCapacity() {
        RingBuffer rb = new RingBuffer(3);
        for (int i = 0; i < 6; i++) rb.writeInternal(i);
        assertEquals(6L, rb.currentWriteSeq());
    }

    // -------------------------------------------------------------------------
    // RingBuffer — readInternal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("readInternal: returns null for a fresh reader on empty buffer")
    void testReadOnEmptyBufferReturnsNull() {
        RingBuffer rb = new RingBuffer(4);
        Reader r = new Reader(rb);
        assertNull(rb.readInternal(r));
    }

    @Test
    @DisplayName("readInternal: returns written value after one write")
    void testReadAfterOneWrite() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(42);
        Reader r = new Reader(rb);
        assertEquals(42, rb.readInternal(r));
    }

    @Test
    @DisplayName("readInternal: reading twice returns consecutive values")
    void testReadReturnsConsecutiveValues() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(1);
        rb.writeInternal(2);
        Reader r = new Reader(rb);
        assertEquals(1, rb.readInternal(r));
        assertEquals(2, rb.readInternal(r));
    }

    @Test
    @DisplayName("readInternal: returns null when reader has caught up")
    void testReadReturnsNullWhenNothingNew() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(5);
        Reader r = new Reader(rb);
        rb.readInternal(r); // reads 5
        assertNull(rb.readInternal(r)); // nothing new
    }

    @Test
    @DisplayName("readInternal: two independent readers read same data independently")
    void testTwoReadersAreIndependent() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(100);
        rb.writeInternal(200);
        Reader r1 = new Reader(rb);
        Reader r2 = new Reader(rb);

        assertEquals(100, rb.readInternal(r1));
        assertEquals(100, rb.readInternal(r2)); // r2 unaffected by r1
        assertEquals(200, rb.readInternal(r1));
        assertEquals(200, rb.readInternal(r2));
    }

    @Test
    @DisplayName("readInternal: reader created mid-stream starts from oldest available slot")
    void testReaderCreatedAfterWritesStartsFromOldest() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(10);
        rb.writeInternal(20);
        rb.writeInternal(30);
        // 3 items written, capacity=4, oldest = seq 0 → reader starts at 0
        Reader r = new Reader(rb);
        assertEquals(10, rb.readInternal(r));
    }

    // -------------------------------------------------------------------------
    // Overwrite (wrap-around) behavior
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Overwrite: reader too slow gets fast-forwarded to oldest available")
    void testSlowReaderJumpsForwardOnOverwrite() {
        RingBuffer rb = new RingBuffer(3); // capacity = 3
        rb.writeInternal(1);
        rb.writeInternal(2);
        rb.writeInternal(3);
        Reader r = new Reader(rb); // starts at seq 0
        // Now overwrite: write 3 more values, oldest available becomes seq 3
        rb.writeInternal(4);
        rb.writeInternal(5);
        rb.writeInternal(6);
        // reader.nextSeq (0) < oldest (3) → should be advanced to 3
        Integer first = rb.readInternal(r);
        // Should read seq 3 (value 4), not the overwritten seq 0 (value 1)
        assertEquals(4, first);
    }

    @Test
    @DisplayName("Overwrite: buffer with capacity 1 always has only the latest value")
    void testCapacityOneAlwaysHoldsLatest() {
        RingBuffer rb = new RingBuffer(1);
        rb.writeInternal(10);
        rb.writeInternal(20);
        rb.writeInternal(30);
        Reader r = new Reader(rb);
        assertEquals(30, rb.readInternal(r));
        assertNull(rb.readInternal(r));
    }

    @Test
    @DisplayName("Overwrite: writeSeq keeps growing monotonically through many overwrites")
    void testWriteSeqMonotonicallyIncreases() {
        RingBuffer rb = new RingBuffer(2);
        for (int i = 0; i < 100; i++) {
            rb.writeInternal(i);
        }
        assertEquals(100L, rb.currentWriteSeq());
    }

    // -------------------------------------------------------------------------
    // Reader — constructor / nextSeq initialization
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Reader: nextSeq starts at 0 when buffer is empty")
    void testReaderNextSeqOnEmptyBuffer() {
        RingBuffer rb = new RingBuffer(4);
        Reader r = new Reader(rb);
        // nextSeq = max(0, 0 - 4) = 0
        // Verify indirectly: first write after reader creation should be readable
        rb.writeInternal(99);
        assertEquals(99, rb.readInternal(r));
    }

    @Test
    @DisplayName("Reader: nextSeq clamps to oldest when buffer is full at creation")
    void testReaderStartsAtOldestWhenBufferFull() {
        RingBuffer rb = new RingBuffer(3);
        rb.writeInternal(10); // seq 0
        rb.writeInternal(20); // seq 1
        rb.writeInternal(30); // seq 2
        // writeSeq=3, oldest = max(0, 3-3) = 0
        Reader r = new Reader(rb);
        assertEquals(10, rb.readInternal(r));
        assertEquals(20, rb.readInternal(r));
        assertEquals(30, rb.readInternal(r));
        assertNull(rb.readInternal(r));
    }

    // -------------------------------------------------------------------------
    // Writer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Writer: write() causes value to be readable by a subsequent Reader")
    void testWriterWriteIsReadable() {
        RingBuffer rb = new RingBuffer(4);
        Writer w = new Writer(rb);
        w.write(77);
        Reader r = new Reader(rb);
        assertEquals(77, rb.readInternal(r));
    }

    @Test
    @DisplayName("Writer: multiple writes are all readable in order")
    void testWriterMultipleWritesInOrder() {
        RingBuffer rb = new RingBuffer(4);
        Writer w = new Writer(rb);
        w.write(1);
        w.write(2);
        w.write(3);
        Reader r = new Reader(rb);
        assertEquals(1, rb.readInternal(r));
        assertEquals(2, rb.readInternal(r));
        assertEquals(3, rb.readInternal(r));
    }

    @Test
    @DisplayName("Writer: write increments RingBuffer's writeSeq")
    void testWriterIncrementsWriteSeq() {
        RingBuffer rb = new RingBuffer(4);
        Writer w = new Writer(rb);
        assertEquals(0L, rb.currentWriteSeq());
        w.write(5);
        assertEquals(1L, rb.currentWriteSeq());
    }

    // -------------------------------------------------------------------------
    // Integration — combined Writer + multiple Readers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Integration: two readers behind a writer, both get all values")
    void testTwoReadersBothGetAllValues() {
        RingBuffer rb = new RingBuffer(4);
        Writer w = new Writer(rb);
        Reader r1 = new Reader(rb);
        Reader r2 = new Reader(rb);

        w.write(10);
        w.write(20);
        w.write(30);

        assertEquals(10, rb.readInternal(r1));
        assertEquals(20, rb.readInternal(r1));
        assertEquals(30, rb.readInternal(r1));
        assertNull(rb.readInternal(r1));

        assertEquals(10, rb.readInternal(r2));
        assertEquals(20, rb.readInternal(r2));
        assertEquals(30, rb.readInternal(r2));
        assertNull(rb.readInternal(r2));
    }

    @Test
    @DisplayName("Integration: reader created after writes only sees available window")
    void testLateReaderSeesOnlyAvailableWindow() {
        RingBuffer rb = new RingBuffer(3);
        Writer w = new Writer(rb);
        w.write(1);
        w.write(2);
        w.write(3);
        w.write(4); // overwrites oldest; buffer holds 2,3,4
        Reader r = new Reader(rb);
        // oldest = max(0, 4-3) = 1 → seq 1 maps to value 2
        assertEquals(2, rb.readInternal(r));
        assertEquals(3, rb.readInternal(r));
        assertEquals(4, rb.readInternal(r));
        assertNull(rb.readInternal(r));
    }

    @Test
    @DisplayName("Integration: zero value is stored and read correctly (not confused with null)")
    void testZeroValueIsReadCorrectly() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(0);
        Reader r = new Reader(rb);
        Integer result = rb.readInternal(r);
        assertNotNull(result);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Integration: negative values are stored and read correctly")
    void testNegativeValueIsReadCorrectly() {
        RingBuffer rb = new RingBuffer(4);
        rb.writeInternal(-99);
        Reader r = new Reader(rb);
        assertEquals(-99, rb.readInternal(r));
    }

    @Test
    @DisplayName("Integration: large number of writes and reads are consistent")
    void testLargeWriteAndReadCycle() {
        int capacity = 5;
        RingBuffer rb = new RingBuffer(capacity);
        Reader r = new Reader(rb);
        for (int i = 0; i < 50; i++) {
            rb.writeInternal(i);
            assertEquals(i, rb.readInternal(r));
            assertNull(rb.readInternal(r));
        }
    }

    // -------------------------------------------------------------------------
    // Documented non-testable / design observations
    // -------------------------------------------------------------------------
    /*
     * PR DOCUMENTATION — Items NOT tested due to source code constraints:
     *
     * 1. RingBuffer.buffer (private, no getter): The actual ArrayList contents
     *    cannot be directly asserted. Buffer state is inferred through Reader reads.
     *
     * 2. RingBuffer.w (write index, private, no getter): Cannot assert wrap-around
     *    of the physical write index directly. Tested indirectly via read correctness.
     *
     * 3. Writer.write() stdout side-effect: Prints "Writer wrote: x" to System.out.
     *    This output is not verified in these tests. To test it, System.out would need
     *    to be redirected — which would require modifying test infrastructure, not source.
     *
     * 4. Reader.readAndPrint() stdout side-effect: Prints to System.out. The return
     *    value is void, so functional correctness is verified through readInternal()
     *    directly in the RingBuffer tests above.
     *
     * 5. RingBuffer.printState(): Purely a diagnostic/print method with no return value.
     *    It cannot be meaningfully unit-tested without stdout capture.
     *
     * 6. Thread safety: The RingBuffer is not synchronized. Concurrent Writer/Reader
     *    scenarios are not tested as this would require source modifications for safety.
     */
}