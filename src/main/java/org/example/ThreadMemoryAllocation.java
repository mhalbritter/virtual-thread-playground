package org.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Moritz Halbritter
 */
class ThreadMemoryAllocation {
	public static void main(String[] args) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		int counter = 0;
		Lock lock = new ReentrantLock();
		lock.lock();

		while (true) {
			counter++;
			int finalCounter = counter;
			Thread thread = new Thread(() -> {
				System.out.println("Thread " + finalCounter);
				lock.lock();
				lock.unlock();
			}, "some-thread-" + counter);
			threads.add(thread);
			// thread.start();
			System.out.println("Created thread " + counter);
			Thread.sleep(Duration.ofMillis(10));
		}
	}
}
