package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import static org.example.VirtualThreadAssert.assertThatCode;

/**
 * @author Moritz Halbritter
 */
class SomeTest {
	@Test
	void pin() throws Exception {
		assertThatCode(this::pinning).doesNotPin();
	}

	@Test
	void doesNotPin() throws Exception {
		assertThatCode(this::noPinning).doesNotPin();
	}

	@Test
	void exceptionIsRethrown() throws Exception {
		assertThatCode(this::throwsException).doesNotPin();
	}

	private void throwsException() {
		throw new RuntimeException("Boom");
	}

	private void noPinning() {
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			Thread.sleep(1);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			lock.unlock();
		}

	}

	private void pinning() {
		Object monitor = new Object();
		synchronized (monitor) {
			try {
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
