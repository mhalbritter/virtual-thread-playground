package org.example;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

/**
 * @author Moritz Halbritter
 */
class JfrPinning {

	private static final Duration RUN_FOR = Duration.ofSeconds(20);

	private static final Duration SLEEP = Duration.ofMillis(1);

	private static final boolean PRINT_ON_LOCK = false;

	private final Object monitor = new Object();

	private final Lock lock = new ReentrantLock();


	public static void main(String[] args) throws Exception {
		JfrPinning jfrPinning = new JfrPinning();
		Configuration configuration = Configuration.create(Path.of("./config.jfc"));
		try (RecordingStream rs = new RecordingStream(configuration)) {
			// See https://openjdk.org/jeps/425#JDK-Flight-Recorder-JFR
			rs.onEvent("jdk.VirtualThreadPinned", jfrPinning::onPinning);
			rs.startAsync();
			jfrPinning.run();
		}
	}

	private void onPinning(RecordedEvent recordedEvent) {
		System.out.printf("Thread %s pinned for %d ms!%n", recordedEvent.getThread().getJavaName(), recordedEvent.getDuration().toMillis());
	}

	private void run() throws Exception {
		for (int i = 0; i < 5; i++) {
			Thread.ofVirtual().name("vt-" + i).start(this::threadMainSynchronized);
		}
		Thread.sleep(RUN_FOR);
	}

	private void threadMainSynchronized() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				synchronized (monitor) {
					if (PRINT_ON_LOCK) {
						System.out.println(Thread.currentThread().getName() + ": Got monitor");
					}
					Thread.sleep(SLEEP);
				}
				Thread.yield();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void threadMainLock() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				lock.lock();
				try {
					if (PRINT_ON_LOCK) {
						System.out.println(Thread.currentThread().getName() + ": Got lock");
					}
					Thread.sleep(SLEEP);
				}
				finally {
					lock.unlock();
				}
				Thread.yield();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
