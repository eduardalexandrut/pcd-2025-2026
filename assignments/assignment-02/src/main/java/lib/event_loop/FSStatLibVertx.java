package lib.event_loop;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import lib.FSStatLib;
import lib.FSStats;
import lib.FSUpdateListener;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FSStatLibVertx implements FSStatLib {
    private final int THROTTLE_SIZE = 500;
    private Vertx vertx;
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
        this.vertx = Vertx.vertx();

        FileSystem fs = vertx.fileSystem();
        fs.readDir(String.valueOf(dir)).onComplete(res -> {
            scanDirectory(dir);
        });
//        this.vertx.deployVerticle(new MyReactiveAgent(), new DeploymentOptions().setWorkerPoolSize(workerPoolSize));
    }

    @Override
    public void stop() {

    }

    private void scanDirectory(Path dir) {
        if (isCompleted.get()) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (isCompleted.get()) {return;}

                if (Files.isDirectory(path)) {
//                    submitTask(() -> scanDirectory(path));
                } else {
//                    submitTask(() -> processFile(path));
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

    private void complete() {
        if (!isCompleted.getAndSet(true)) {
            try {
                listener.onComplete(stats.snapshot());
            } finally {
//                executor.shutdown();
            }
        }
    }
}
