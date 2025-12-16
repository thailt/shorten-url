package com.trithai.utils.shortenurl.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WriteBehindBuffer {

    private final BlockingQueue<AliasWriteTask> writeQueue = new LinkedBlockingQueue<>();

    public void enqueue(String alias, String url, LocalDateTime expire) {
        LocalDateTime expiredAt =
                Objects.requireNonNullElse(expire, LocalDateTime.now().plusYears(1));
        AliasWriteTask task =
                AliasWriteTask.builder()
                        .alias(alias)
                        .url(url)
                        .createdAt(LocalDateTime.now())
                        .expiredAt(expiredAt)
                        .build();
        writeQueue.offer(task);
        log.debug("Enqueued alias for write-behind: {}", alias);
    }

    public List<AliasWriteTask> drain() {
        List<AliasWriteTask> batch = new ArrayList<>();
        writeQueue.drainTo(batch);
        if (!batch.isEmpty()) {
            log.info("Draining {} aliases from write-behind buffer", batch.size());
        }
        return batch;
    }

    public int size() {
        return writeQueue.size();
    }
}

