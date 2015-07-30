package simple.util.concurrent.locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple, compact SeqLock implementation for Java.
 * <p>
 *
 * <b>Reader Thread:</b>
 * <pre>
 * for (;;) {
 *     final long status = seqLock.readBegin();
 *     // ... some read operation ...
 *     // ... some read operation ...
 *     if (seqLock.checkRead(status)) break;
 * }
 * </pre>
 *
 * <b>Writer Thread:</b>
 * <pre>
 * seqLock.writeLock();
 * try {
 *     // ... some write operation ...
 *     // ... some write operation ...
 * } finally {
 *     seqLock.writeUnlock();
 * }
 * </pre>
 *
 * <p>
 * Another useful trick for the reader thread is to check in between whether the
 * read operation is still valid and restart the entire read operation if not.
 * <pre>
 * loop: for (;;) {
 *     final long status = seqLock.readBegin();
 *     // ... some read operation ...
 *     // ... some read operation ...
 *     
 *     if (seqLock.readRetry(status))
 *         continue loop; // restart if read is no longer invalid
 *     
 *     // ... some read operation ...
 *     // ... some read operation ...
 *     if (seqLock.checkRead(status)) break;
 * }
 * </pre>
 */
public class SimpleSeqLock {
	private volatile long status;
	private final Lock writeLock;

	public SimpleSeqLock() {
		this.writeLock = new ReentrantLock();
	}

	public SimpleSeqLock(Lock writeLock) {
		this.writeLock = writeLock;
	}

	public long readBeginSpin() {
		for (;;) {
			long current = status;
			if ((current & 1) == 0)
				return current;
		}
	}

	/**
	 * This is the recommended method if the lock used is a {@code ReentrantLock},
	 * as it handles the optimal spinlock.
	 * 
	 * @see java.util.concurrent.locks.ReentrantLock
	 */
	public long readBegin() {
		for (;;) {
			long current = status;
			if ((current & 1) == 0)
				return current;
			writeLock.lock(); // Avoids spin lock by halting until lock-acquisition.
			writeLock.unlock();
		}
	}

	public boolean checkRead(long current) {
		return current == status;
	}

	/**
	 * Same as ({@code !checkRead(value)})
	 * 
	 * @see #checkRead(long)
	 */
	public boolean readRetry(long current) {
		return current != status;
	}

	public void writeLock() {
		writeLock.lock();
		status++; // No need to use Java's Atomic increments here
	}

	public void writeUnlock() {
		status++;
		writeLock.unlock();
	}

	public void writeLockInterruptibly() throws InterruptedException {
		writeLock.lockInterruptibly(); // If we get interrupted, do not proceed below.
		status++; // Increment only on successful uninterrupted lock.
	}
}
