package org.example;

import java.time.Duration;

/**
 * @author Moritz Halbritter
 */
class PinningExhaustsCarriers {
	public static void main(String[] args) throws InterruptedException {
		System.setProperty("jdk.tracePinnedThreads", "short"); // Or "full"

		System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "256");

		for (int i = 0; i < 5; i++) {
			int finalI = i;
			Thread.ofVirtual().start(() -> threadMain(finalI));
		}

		for (int i = 0; i < 10; i++) {
			int finalI = i;
			Thread.ofVirtual().start(() -> System.out.println("Some other virtual thread " + finalI));
		}

		Thread.sleep(Duration.ofSeconds(10));
	}

	private static final Object monitor = new Object();

	private static void threadMain(int num) {
		System.out.printf("Thread %d start%n", num);
		synchronized (monitor) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.printf("Thread %d stop%n", num);
	}
}
