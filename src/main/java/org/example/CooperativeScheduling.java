package org.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CooperativeScheduling {
	public static void main(String[] args) throws InterruptedException {
		System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "1");
		// System.setProperty("jdk.virtualThreadScheduler.minRunnable", "0");
		List<Log> logs = Collections.synchronizedList(new ArrayList<>());
		for (int i = 0; i < 10; i++) {
			int finalI = i;
			Thread.ofVirtual().start(() -> threadMain(logs, finalI));
		}
		Thread.sleep(Duration.ofSeconds(5));
		logs.stream().sorted(Comparator.comparing(Log::nanos)).forEach(log -> System.out.printf("%d: %s%n", log.nanos(), log.message()));
	}

	private static void threadMain(List<Log> logs, int num) {
		logs.add(Log.format("Thread %d started", num));
		for (int i = 0; i < 10000000; i++) {
			if (i % 10000 == 0) {
				// Thread.yield();
			}
		}
		logs.add(Log.format("Thread %d stopped", num));
	}

	record Log(long nanos, String message) {
		private static final long started = System.nanoTime();

		static Log format(String message, Object... args) {
			return new Log(System.nanoTime() - started, message.formatted(args));
		}
	}
}
