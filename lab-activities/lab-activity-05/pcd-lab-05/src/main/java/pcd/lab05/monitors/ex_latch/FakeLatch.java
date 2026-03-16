package pcd.lab05.monitors.ex_latch;

/*
 * Latch - to be implemented
 */
public class FakeLatch implements Latch {
	private int count;
	public FakeLatch(final int count) {
        this.count = Math.max(count,0);
    }
	
	@Override
	public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }

	@Override
	public synchronized void countDown() {
        if (count > 0) {
            this.count--;

            if (this.count == 0){
                notifyAll();
            }
        }
    }

	
}
