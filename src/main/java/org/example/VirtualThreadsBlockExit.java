package org.example;

/**
 * @author Moritz Halbritter
 */
class VirtualThreadsBlockExit {
	public static void main(String[] args) {
		Thread thread = Thread.ofVirtual().unstarted(() -> {
			System.out.println("Thread start");
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Thread stop");
		});
		thread.setDaemon(false);
		thread.start();
		System.out.println("Main stop");
	}
}
