package lib;

public class FSStats {
    private long totalFiles;
    private long[] bands;

    public FSStats(int nb) {
        this.totalFiles = 0;
        this.bands = new long[nb + 1];
    }

    public synchronized FSReport snapshot() {
        return new FSReport(totalFiles, bands.clone());
    }

    public synchronized void addFile(int band) {
        this.totalFiles += 1;
        this.bands[band] += 1;
    }

    public long getTotalFiles() {
        return this.totalFiles;
    }
}
