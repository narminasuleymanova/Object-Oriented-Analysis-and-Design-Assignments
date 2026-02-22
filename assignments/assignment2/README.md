# Ring Buffer – Multiple Readers, Single Writer

## Project Overview

This project implements a **Ring Buffer** (circular buffer) in Java that supports a **single writer** and **multiple independent readers**. Each reader maintains its own position in the buffer, so one reader reading or falling behind does not affect the others. When the buffer is full, the writer overwrites the oldest data, and any reader that has fallen too far behind will automatically skip ahead to the oldest still-available item.

---

## Design

The solution follows object-oriented principles by splitting responsibilities across four classes:

### `RingBuffer`
The core data structure. Holds the fixed-capacity array, the current write index, and a monotonically increasing `writeSeq` counter. Provides `writeInternal(int)` and `readInternal(Reader)` as the low-level operations, plus a `printState()` helper for debugging. It does **not** know about business logic — it just manages the buffer mechanics.

### `Writer`
Encapsulates the single-writer role. Holds a reference to the `RingBuffer` and exposes a clean `write(int)` method. All writes go through this class, enforcing the single-writer constraint at the design level.

### `Reader`
Encapsulates one independent reading position. Each `Reader` instance stores its own `nextSeq` — the sequence number of the next item it wants to read. Readers are created independently and start from the oldest available item in the buffer at the time of construction. Exposes `readAndPrint(String)` for reading and printing results.

### `Main`
The entry point / driver class. Demonstrates the buffer behavior: writing items, reading independently from two readers, and observing overwrite behavior when the buffer fills up.

---

## UML Class Diagram

```
┌─────────────────────────────────────────┐
│               RingBuffer                │
├─────────────────────────────────────────┤
│ - buffer: List<Integer>                 │
│ - n: int                                │
│ - w: int                                │
│ - writeSeq: long                        │
├─────────────────────────────────────────┤
│ + RingBuffer(n: int)                    │
│ ~ writeInternal(x: int): void           │
│ ~ readInternal(r: Reader): Integer      │
│ + currentWriteSeq(): long               │
│ + capacity(): int                       │
│ + printState(title, readers...): void   │
└──────────────┬──────────────────────────┘
               │ uses
       ┌───────┴──────────┐
       │                  │
┌──────▼────────┐  ┌──────▼────────┐
│    Writer     │  │    Reader     │
├───────────────┤  ├───────────────┤
│ - rb:         │  │ ~ rb:         │
│   RingBuffer  │  │   RingBuffer  │
├───────────────┤  │ ~ nextSeq:    │
│ + Writer(rb)  │  │   long        │
│ + write(x)    │  ├───────────────┤
└───────────────┘  │ + Reader(rb)  │
                   │ + readAndPrint│
                   │   (name)      │
                   └───────────────┘
```

---

## UML Sequence Diagram – `write()`

```
  Main            Writer          RingBuffer
   │                │                  │
   │  write(10)     │                  │
   │───────────────>│                  │
   │                │  writeInternal(10)
   │                │─────────────────>│
   │                │                  │  buffer[w] = 10
   │                │                  │  w = (w+1) % n
   │                │                  │  writeSeq++
   │                │<─────────────────│
   │<───────────────│                  │
```

---

## UML Sequence Diagram – `read()`

```
  Main            Reader          RingBuffer
   │                │                  │
   │ readAndPrint() │                  │
   │───────────────>│                  │
   │                │  readInternal(this)
   │                │─────────────────>│
   │                │                  │  oldest = max(0, writeSeq - n)
   │                │                  │  if nextSeq < oldest: nextSeq = oldest
   │                │                  │  if nextSeq >= writeSeq: return null
   │                │                  │  idx = nextSeq % n
   │                │                  │  value = buffer[idx]
   │                │                  │  nextSeq++
   │                │<─────────────────│  return value
   │  print result  │                  │
   │<───────────────│                  │
```

---

## How to Run / Test

### Prerequisites
- Java 11 or higher
- No external dependencies required

### Running from the command line

1. Clone the repository:
   ```bash
   git clone <https://github.com/narminasuleymanova/Object-Oriented-Analysis-and-Design-Assignments.git>
   cd <assignments>
   ```

2. Compile all source files:
   ```bash
   javac assignment2/*.java
   ```

3. Run the main class:
   ```bash
   java assignment2.Main
   ```

### Expected output

The program will show:
- Each `write()` call printing the written value
- Buffer state snapshots (buffer contents, write index, sequence numbers, reader positions)
- Each `read()` call printing the value read, or `"nothing new"` if the reader is caught up
- Demonstration of overwrite behavior when the buffer is full and a slow reader misses items

### Testing different scenarios

To test other scenarios, modify `Main.java`:
- Add more `Reader` instances to test additional independent readers
- Write more items than the buffer capacity to observe overwrite/skip behavior
- Create a `Reader` after some writes to verify it starts from the oldest available item
