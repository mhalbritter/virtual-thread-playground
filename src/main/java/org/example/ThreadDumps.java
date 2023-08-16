package org.example;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat;

/**
 * @author Moritz Halbritter
 */
class ThreadDumps {
	public static void main(String[] args) throws Exception {
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().name("virtual-thread-", 0).factory());
			for (int i = 0; i < 10; i++) {
				executor.submit(() -> {
					lock.lock();
					try {
						System.out.println("Thread " + Thread.currentThread().getName() + " has the lock");
					}
					finally {
						lock.unlock();
					}
				});
			}

			threadDumpViaHotspotDiagnosticsBean();
			threadDumpViaThreadBean();

			System.out.println("Shutting down executor ...");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
			executor.shutdownNow();
		}
		finally {
			lock.unlock();
		}
	}

	private static void threadDumpViaThreadBean() throws IOException {
		System.out.println("Dump threads ...");
		ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
		PlainTextThreadDumpFormatter formatter = new PlainTextThreadDumpFormatter();
		Path file = Path.of("threads.txt").toAbsolutePath();
		Files.writeString(file, formatter.format(threads), StandardCharsets.UTF_8);
		System.out.println("Dumped threads to " + file);
	}

	private static void threadDumpViaHotspotDiagnosticsBean() throws Exception {
		System.out.println("Dump threads ...");
		HotSpotDiagnosticMXBean bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
		if (bean == null) {
			throw new IllegalStateException("No HotSpotDiagnosticMXBean found");
		}
		Path file = Path.of("threads.json").toAbsolutePath();
		Files.deleteIfExists(file);
		bean.dumpThreads(file.toString(), ThreadDumpFormat.JSON);
		System.out.println("Dumped threads to " + file);
	}
}
