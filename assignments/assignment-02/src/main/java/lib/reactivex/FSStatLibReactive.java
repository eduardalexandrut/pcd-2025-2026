package lib.reactivex;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import lib.FSStatLib;
import lib.FSStats;
import lib.FSUpdateListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FSStatLibReactive implements FSStatLib {
    private Vertx vertx;
    private FSUpdateListener listener;
    private Path dir;
    private long maxFS;
    private int nb;
    private long bandSize;

    private FSStats stats;
    private Disposable disposable;

    private final AtomicBoolean isCompleted = new AtomicBoolean(false);
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    @Override
    public void getFSReport(Path dir, long maxFS, int nb, FSUpdateListener listener) {
        this.vertx = Vertx.vertx();

        this.listener = listener;
        this.nb = nb;
        this.maxFS = maxFS;
        this.bandSize = maxFS / nb;


        this.stats = new FSStats(nb);

        Flowable<Path> files = scanDirectory(dir).subscribeOn(Schedulers.io());

        disposable = files
                .map(Files::size)
                .map(this::computeBand)
                .doOnNext(stats::addFile)
                .doOnNext(r -> listener.onUpdate(stats.snapshot()))
                .doOnComplete(() -> listener.onComplete(stats.snapshot()))
                .buffer(300)
                .subscribe();
    }

    @Override
    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private Flowable<Path> scanDirectory(Path dir) {
       return Flowable.create(emitter -> {
            if (emitter.isCancelled()) {return;}

            FileSystem fs = vertx.fileSystem();

            fs.readDir(dir.toString()).onComplete(result -> {
                if (emitter.isCancelled()) {return;}

                if (result.failed()) {
                    emitter.onError(result.cause());
                    return;
                }

                for (final String entry : result.result()) {

                    fs.props(entry).onComplete(propRes -> {

                        if (emitter.isCancelled()) {return;}

                        if (propRes.failed()) {
                            emitter.onError(propRes.cause());
                            return;
                        }

                        if (propRes.result().isDirectory()) {
                            scanDirectory(Path.of(entry)).subscribe(emitter::onNext, emitter::onError);
                        } else {
                            emitter.onNext(Path.of(entry));
                        }
                    });
                }
            });
       }, BackpressureStrategy.BUFFER);
    }

    private int computeBand(long fileSize) {
        int band = (int) (fileSize / bandSize);

        if (band >= nb)
            band = nb;

        return band;
    }

}
