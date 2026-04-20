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
    private static final int DEFAULT_CONCURRENCY = 8;

    private FSUpdateListener listener;
    private Path dir;
    private long maxFS;
    private int nb;
    private long bandSize;

    private FSStats stats;
    private Disposable disposable;

    @Override
    public void getFSReport(Path dir, long maxFS, int nb, FSUpdateListener listener) {

        this.listener = listener;
        this.nb = nb;
        this.maxFS = maxFS;
        this.bandSize = maxFS / nb;


        this.stats = new FSStats(nb);

        disposable = scanDirectory(dir)
                .map(Files::size)
                .map(size -> Math.min((int) (size / bandSize), nb))
                .doOnNext(stats::addFile)
                .buffer(200, TimeUnit.MILLISECONDS)
                .filter(batch -> !batch.isEmpty())
                .doOnNext(batch -> listener.onUpdate(stats.snapshot()))
                .doOnComplete(() -> listener.onComplete(stats.snapshot()))
                .subscribe();
    }

//    private record SizedPath(Path path, long size) {}

    @Override
    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

//    private Flowable<SizedPath> scanDirectory(Path dir) {
//        FileSystem fs = vertx.fileSystem();
//
//        return Single.fromCompletionStage(fs.readDir(dir.toString()).toCompletionStage())
//                .flatMapPublisher(entries -> {
//                    List<Flowable<SizedPath>> flowables = entries.stream()
//                            .map(entry -> Single.fromCompletionStage(
//                                            fs.props(entry).toCompletionStage())
//                                    .flatMapPublisher(props -> {
//                                        if (props.isDirectory()) {
//                                            return scanDirectory(Path.of(entry));
//                                        } else {
//                                            return Flowable.just(new SizedPath(Path.of(entry), props.size()));
//                                        }
//                                    }))
//                            .toList();
//                    return Flowable.merge(flowables, 8);
//                });
//    }
    private Flowable<Path> scanDirectory(Path dir) {
        return Single.fromCallable(() -> {
                    try (var stream = Files.list(dir)) {
                        return stream.toList();
                    }
                })
                .subscribeOn(Schedulers.io())        // readDir on I/O thread
                .flatMapPublisher(entries -> Flowable.fromIterable(entries)
                        .flatMap(entry -> Single.fromCallable(() -> entry)
                                .subscribeOn(Schedulers.io())
                                .flatMapPublisher(path -> {
                                    if (Files.isDirectory(path)) {
                                        return scanDirectory(path);   // recurse
                                    } else {
                                        return Flowable.just(path);
                                    }
                                }), DEFAULT_CONCURRENCY
                        )
                );
    }

}
