package org.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import org.assertj.core.api.AbstractAssert;
import org.example.VirtualThreadAssert.ThrowingRunnable;

/**
 * @author Moritz Halbritter
 */
class VirtualThreadAssert extends AbstractAssert<VirtualThreadAssert, ThrowingRunnable> {
	private static final Object configurationLock = new Object();

	private static Configuration configuration;

	protected VirtualThreadAssert(ThrowingRunnable runnable) {
		super(runnable, VirtualThreadAssert.class);
	}

	static VirtualThreadAssert assertThatCode(ThrowingRunnable runnable) {
		return new VirtualThreadAssert(runnable);
	}

	void doesNotPin() throws Exception {
		isNotNull();
		Configuration configuration = getOrCreateJfrConfiguration();
		Queue<RecordedEvent> events = new ConcurrentLinkedQueue<>();
		try (RecordingStream rs = new RecordingStream(configuration)) {
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
	}

	private static Configuration getOrCreateJfrConfiguration() {
		synchronized (configurationLock) {
			if (configuration == null) {
				try (Reader reader = new InputStreamReader(VirtualThreadAssert.class.getResourceAsStream("/virtual-thread-pinning.jfc"), StandardCharsets.UTF_8)) {
					configuration = Configuration.create(reader);
				}
				catch (ParseException ex) {
					throw new IllegalStateException("Failed to parse config", ex);
				}
				catch (IOException ex) {
					throw new UncheckedIOException(ex);
				}
			}
			return configuration;
		}
	}

	@FunctionalInterface
	interface ThrowingRunnable {
		void run() throws Exception;
	}
}
