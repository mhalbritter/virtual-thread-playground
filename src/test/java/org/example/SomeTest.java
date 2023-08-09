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
		assertThatCode(SomeTest::pinning).doesNotPin();
	}

	@Test
	void doesNotPin() throws Exception {
		assertThatCode(SomeTest::noPinning).doesNotPin();
	}

	private static void noPinning() throws InterruptedException {
		Lock lock = new ReentrantLock();
		Thread.ofVirtual().name("vt-0").start(() -> {
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
		).join();
	}

	private static void pinning() throws InterruptedException {
		Object monitor = new Object();
		Thread.ofVirtual().name("vt-0").start(() -> {
					synchronized (monitor) {
						try {
							Thread.sleep(1);
						}
						catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
		).join();
	}
}
