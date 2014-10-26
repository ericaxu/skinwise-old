package src.util;

import java.util.concurrent.Executor;

public class JoinableExecutor implements Executor {
	private Executor executor;
	private int counter;

	public JoinableExecutor(Executor executor) {
		this.executor = executor;
		this.counter = 0;
	}

	public void execute(Runnable command) {
		synchronized (this) {
			counter++;
		}
		executor.execute(new CounterRunnable(command, this));
	}

	public void runnableExit() {
		synchronized (this) {
			counter--;
			this.notify();
		}
	}

	public void join() throws InterruptedException {
		synchronized (this) {
			while (counter > 0) {
				this.wait();
			}
		}
	}

	private static class CounterRunnable implements Runnable {
		private Runnable runnable;
		private JoinableExecutor executor;

		public CounterRunnable(Runnable runnable, JoinableExecutor executor) {
			this.runnable = runnable;
			this.executor = executor;
		}

		@Override
		public void run() {
			try {
				runnable.run();
			}
			finally {
				executor.runnableExit();
			}
		}
	}
}
