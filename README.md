# Java SeqLock

A simple, compact SeqLock implementation for Java.

The implementation details were taken from David Dice's Weblog [here][1].

This is an improved SeqLock implementation taken from my SO [post][2] years ago.

# Usage

__Reader Thread:__
```java
for (;;) {
	final long status = seqLock.readBegin();
	// ... some read operation ...
	// ... some read operation ...
	if (seqLock.checkRead(status)) break;
}
```

__Writer Thread:__
```java
seqLock.writeLock();
try {
	// ... some write operation ...
	// ... some write operation ...
} finally {
	seqLock.writeUnlock();
}
```

<br>
Another useful trick for the reader thread is to check in between whether the
read operation is still valid and restart the entire read operation if not.
```java
loop: for (;;) {
	final long status = seqLock.readBegin();
	// ... some read operation ...
	// ... some read operation ...

	if (seqLock.readRetry(status))
		continue loop; // restart if read is no longer valid

	// ... some read operation ...
	// ... some read operation ...
	if (seqLock.checkRead(status)) break loop;
}
```

  [1]: https://blogs.oracle.com/dave/entry/seqlocks_in_java
  [2]: http://stackoverflow.com/q/19577909/1906724