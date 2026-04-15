package lib.virtual_threads;

import lib.FSReport;
import lib.FSStatLib;
import lib.FSStats;
import lib.FSUpdateListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FSStatLibVT implements FSStatLib {

    private ExecutorService executor;
    private FSUpdateListener listener;
    private Path dir;
    private long maxFS;
    private int nb;
    private int bandSize;

    private FSStats report;

    private final AtomicBoolean isCompleted = new AtomicBoolean(false);

    @Override
    public void getFSReport(Path dir, long maxFS, int nb, FSUpdateListener listener) {
        this.dir = dir;
        this.maxFS = maxFS;
        this.nb = nb;
        this.bandSize = (int) (maxFS / nb);
        this.listener = listener;
        this.isCompleted.set(false);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        this.executor.submit(() -> scanDirectory(this.dir));

        this.executor.submit(() -> {
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}

            listener.onComplete(report.snapshot());
        });
    }

    @Override
    public void stop() {
        this.isCompleted.set(true);

        if (this.executor != null) {
            this.executor.shutdown();
        }
    }

    private void scanDirectory(Path dir) {
        if (isCompleted.get()) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (isCompleted.get()) {return;}

                if (Files.isDirectory(path)) {
                    this.executor.submit(() -> scanDirectory(path));
                } else {
                    executor.submit(() -> processFile(path));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processFile(Path path) {
        if (isCompleted.get()) {return;}

        try {
            final long fileSize = Files.size(path);
            final int band = computeBand(fileSize);

            report.addFile(band);
            listener.onUpdate(report.snapshot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int computeBand(long fileSize) {
        int band = (int) (fileSize / bandSize);

        if (band >= nb)
            band = nb;

        return band;
    }
}
