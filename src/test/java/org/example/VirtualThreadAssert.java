package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import org.assertj.core.api.AbstractAssert;
import org.example.VirtualThreadAssert.ThrowingRunnable;

/**
 * @author Moritz Halbritter
 */
final class VirtualThreadAssert extends AbstractAssert<VirtualThreadAssert, ThrowingRunnable> {
	private VirtualThreadAssert(ThrowingRunnable runnable) {
		super(runnable, VirtualThreadAssert.class);
	}

	static VirtualThreadAssert assertThatCode(ThrowingRunnable runnable) {
		return new VirtualThreadAssert(runnable);
	}

	void doesNotPin() throws Exception {
		isNotNull();
		Queue<RecordedEvent> events = new ConcurrentLinkedQueue<>();
		try (RecordingStream rs = new RecordingStream()) {
			rs.setSettings(Map.of(
					"jdk.VirtualThreadPinned#enabled", "true",
					"jdk.VirtualThreadPinned#stackTrace", "true",
					"jdk.VirtualThreadPinned#threshold", "0 ms"
			));
			rs.setReuse(false);
			// See https://openjdk.org/jeps/425#JDK-Flight-Recorder-JFR
			rs.onEvent("jdk.VirtualThreadPinned", events::add);
			rs.startAsync();
			Exception exception = startVirtualThread(actual);
			if (exception != null) {
				throw exception;
			}
			rs.stop();
		}
		if (!events.isEmpty()) {
			StringBuilder details = new StringBuilder();
			for (RecordedEvent event : events) {
				details.append("Thread '%s' pinned for %d ms%n".formatted(event.getThread().getJavaName(), event.getDuration().toMillis()));
				details.append("details = ").append(event).append("\n");
			}
			failWithMessage("Expected no pinning to happen, but found pinning:%n%s", details);
		}
	}

	private Exception startVirtualThread(ThrowingRunnable runnable) {
		try {
			AtomicReference<Exception> thrown = new AtomicReference<>();
			Thread.ofVirtual().name("test-subject").start(() -> {
				try {
					runnable.run();
				}
				catch (Exception ex) {
					thrown.set(ex);
				}
			}).join();
			return thrown.get();
		}
		catch (InterruptedException ex) {
			failWithMessage("Got interrupted while waiting for virtual thread");
		}
		throw new AssertionError("Unreachable");
	}

	@FunctionalInterface
	interface ThrowingRunnable {
		void run() throws Exception;
	}
}
