package lib.reactivex;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import lib.FSStatLib;
import lib.FSStats;
import lib.FSUpdateListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FSStatLibReactive implements FSStatLib {
    private Vertx vertx = Vertx.vertx();
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

        this.listener = listener;
        this.nb = nb;
        this.maxFS = maxFS;
        this.bandSize = maxFS / nb;


        this.stats = new FSStats(nb);

//        Flowable<Path> files = scanDirectory(dir).subscribeOn(Schedulers.io());

        disposable = scanDirectory(dir)
                .map(sizedPath -> Math.min((int) (sizedPath.size() / bandSize), nb))
                .doOnNext(stats::addFile)
                .buffer(200, TimeUnit.MILLISECONDS)
                .doOnNext(r -> listener.onUpdate(stats.snapshot()))
                .doOnComplete(() -> listener.onComplete(stats.snapshot()))
                .subscribe();
    }

    private record SizedPath(Path path, long size) {}

    @Override
    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private Flowable<SizedPath> scanDirectory(Path dir) {
        FileSystem fs = vertx.fileSystem();

        return Single.fromCompletionStage(fs.readDir(dir.toString()).toCompletionStage())
                .flatMapPublisher(entries -> {
                    List<Flowable<SizedPath>> flowables = entries.stream()
                            .map(entry -> Single.fromCompletionStage(
                                            fs.props(entry).toCompletionStage())
                                    .flatMapPublisher(props -> {
                                        if (props.isDirectory()) {
                                            return scanDirectory(Path.of(entry));
                                        } else {

                                            return Flowable.just(new SizedPath(Path.of(entry), props.size()));
                                        }
                                    }))
                            .toList();
                    return Flowable.merge(flowables, 8);
                });
    }

}
