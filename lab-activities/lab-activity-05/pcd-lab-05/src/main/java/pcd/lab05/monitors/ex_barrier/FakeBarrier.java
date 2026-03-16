package pcd.lab05.monitors.ex_barrier;

/*
 * Barrier - to be implemented
 */
public class FakeBarrier implements Barrier {

    private int nParticipants;
    private int confirmed = 0;
	public FakeBarrier(int nParticipants) {
        this.nParticipants = nParticipants;
    }
	
	@Override
	public synchronized void hitAndWaitAll() throws InterruptedException {
        this.confirmed = this.confirmed + 1;

        if (this.confirmed < this.nParticipants) {
            while (this.confirmed < this.nParticipants) {
                wait();
            }
        } else {
            notifyAll();
        }

	}

	
}
