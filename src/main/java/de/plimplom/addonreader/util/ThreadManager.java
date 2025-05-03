package de.plimplom.addonreader.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThreadManager {
    private static final ExecutorService GENERAL_EXECUTOR =
            new ThreadPoolExecutor(2, 5, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(), new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "app-worker-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
            Executors.newScheduledThreadPool(1, r -> {
                Thread thread = new Thread(r, "app-scheduler");
                thread.setDaemon(true);
                return thread;
            });

    public static <T> CompletableFuture<T> runAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, GENERAL_EXECUTOR);
    }

    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, GENERAL_EXECUTOR);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return SCHEDULED_EXECUTOR.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return SCHEDULED_EXECUTOR.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public static void shutdown() {
        GENERAL_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();

        try {
            if (!GENERAL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                GENERAL_EXECUTOR.shutdownNow();
            }
            if (!SCHEDULED_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                SCHEDULED_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            GENERAL_EXECUTOR.shutdownNow();
            SCHEDULED_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
