package org.example;

/**
 * @author Moritz Halbritter
 */
class VirtualShutdownHook {
	public static void main(String[] args) {
		Thread thread = Thread.ofVirtual().unstarted(() -> {
			System.out.println("Thread start. Virtual = " + Thread.currentThread().isVirtual());
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Thread stop");
		});
		Runtime.getRuntime().addShutdownHook(thread);
	}
}
