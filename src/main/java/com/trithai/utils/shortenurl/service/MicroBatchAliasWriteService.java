package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.exceptions.CreatingAliasExistedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MicroBatchAliasWriteService {

    private final MicroBatchFlushHandler flushHandler;
    private final AliasDBService aliasDBService;

    @Value("${app.micro-batch.wait-ms:50}")
    private long waitMs;

    @Value("${app.micro-batch.max-size:1000}")
    private int maxBatchSize;

    private final BlockingQueue<PendingAliasCreate> queue = new LinkedBlockingQueue<>();
    private volatile Thread consumer;
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        consumer =
                Thread.ofPlatform()
                        .name("micro-batch-alias")
                        .daemon(false)
                        .unstarted(this::consumeLoop);
        consumer.start();
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        running = false;
        if (consumer != null) {
            consumer.interrupt();
            consumer.join(10_000);
        }
        flushRemaining();
    }

    public CompletableFuture<Alias> submit(String alias, String url, LocalDateTime expiredAt) {
        LocalDateTime createdAt = LocalDateTime.now();
        CompletableFuture<Alias> result = new CompletableFuture<>();
        queue.offer(new PendingAliasCreate(alias, url, createdAt, expiredAt, result));
        return result;
    }

    public Alias submitAndAwait(String alias, String url, LocalDateTime expiredAt) {
        try {
            return submit(alias, url, expiredAt).join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof CreatingAliasExistedException e) {
                throw e;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(cause);
        }
    }

    private void consumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                PendingAliasCreate first = queue.poll(waitMs, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                List<PendingAliasCreate> batch = new ArrayList<>(maxBatchSize);
                batch.add(first);
                queue.drainTo(batch, maxBatchSize - 1);
                processBatch(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Micro-batch consumer error", e);
            }
        }
    }

    private void flushRemaining() {
        List<PendingAliasCreate> batch = new ArrayList<>();
        queue.drainTo(batch);
        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    private void processBatch(List<PendingAliasCreate> batch) {
        List<Alias> entities = new ArrayList<>(batch.size());
        for (PendingAliasCreate p : batch) {
            entities.add(
                    Alias.builder()
                            .alias(p.alias())
                            .url(p.url())
                            .createdAt(p.createdAt())
                            .expiredAt(p.expiredAt())
                            .build());
        }
        try {
            List<Alias> saved = flushHandler.saveBatch(entities);
            completeInOrder(batch, saved);
        } catch (Exception ex) {
            Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
            if (root instanceof DataIntegrityViolationException) {
                log.warn(
                        "Micro-batch save failed ({} rows): data integrity violation, falling back to per-row saves",
                        batch.size());
                fallbackPerRow(batch);
            } else {
                log.error("Micro-batch save failed for {} rows", batch.size(), ex);
                for (PendingAliasCreate p : batch) {
                    p.result().completeExceptionally(ex);
                }
            }
        }
    }

    private void completeInOrder(List<PendingAliasCreate> batch, List<Alias> saved) {
        if (saved.size() != batch.size()) {
            var err =
                    new IllegalStateException(
                            "Batch size mismatch: expected " + batch.size() + ", got " + saved.size());
            for (PendingAliasCreate p : batch) {
                p.result().completeExceptionally(err);
            }
            return;
        }
        for (int i = 0; i < batch.size(); i++) {
            batch.get(i).result().complete(saved.get(i));
        }
    }

    private void fallbackPerRow(List<PendingAliasCreate> batch) {
        for (PendingAliasCreate p : batch) {
            if (p.result().isDone()) {
                continue;
            }
            try {
                Alias saved =
                        aliasDBService.saveToDb(
                                p.alias(), p.url(), p.expiredAt(), p.createdAt());
                p.result().complete(saved);
            } catch (CreatingAliasExistedException ex) {
                p.result().completeExceptionally(ex);
            } catch (Exception ex) {
                p.result().completeExceptionally(ex);
            }
        }
    }
}
