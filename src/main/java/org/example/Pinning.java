package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Moritz Halbritter
 */
class Pinning {
	public static void main(String[] args) throws InterruptedException {
		System.setProperty("jdk.tracePinnedThreads", "short"); // Or "full"

		Thread.ofVirtual().start(Pinning::threadMainSynchronized);
		Thread.ofVirtual().start(Pinning::threadMainLock);
		Thread.sleep(1000);
	}

	private static final Object monitor = new Object();

	private static final Lock lock = new ReentrantLock();

	private static void threadMainSynchronized() {
		synchronized (monitor) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void threadMainLock() {
		lock.lock();
		try {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		finally {
			lock.unlock();
		}
	}
}
