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
import java.util.concurrent.atomic.AtomicInteger;

public class FSStatLibVT implements FSStatLib {
    private final int THROTTLE_SIZE = 500;

    private ExecutorService executor;
    private FSUpdateListener listener;
    private Path dir;
    private long maxFS;
    private int nb;
    private long bandSize;

    private FSStats stats;

    private final AtomicBoolean isCompleted = new AtomicBoolean(false);
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    @Override
    public void getFSReport(Path dir, long maxFS, int nb, FSUpdateListener listener) {
        this.dir = dir;
        this.maxFS = maxFS;
        this.nb = nb;
        this.bandSize = (maxFS / nb);
        this.listener = listener;
        this.isCompleted.set(false);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        this.stats = new FSStats(nb);

        submitTask(() -> scanDirectory(this.dir));

    }

    @Override
    public void stop() {
        if (!this.isCompleted.getAndSet(true)) {
            if (this.executor != null) {
                executor.shutdownNow();
            }
            listener.onComplete(stats.snapshot());
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
                    submitTask(() -> scanDirectory(path));
                } else {
                    submitTask(() -> processFile(path));
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

            stats.addFile(band);
            if (stats.getTotalFiles() % THROTTLE_SIZE == 0) {
                listener.onUpdate(stats.snapshot());
            }
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

    private void submitTask(Runnable task) {
        if (this.isCompleted.get()) {return;}

        activeTasks.incrementAndGet();

        executor.submit(() -> {
            try {
                task.run();
            } finally {
                if (activeTasks.decrementAndGet() == 0) {
                    complete();
                }
            }
        });
    }

    private void complete() {
        if (!isCompleted.getAndSet(true)) {
            try {
                listener.onComplete(stats.snapshot());
            } finally {
                executor.shutdown();
            }
        }
    }
}
