package com.trithai.utils.shortenurl.config;

import com.trithai.utils.shortenurl.service.AliasWriteTask;
import com.trithai.utils.shortenurl.service.BatchWriteService;
import com.trithai.utils.shortenurl.service.WriteBehindBuffer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class WriteBehindScheduler {

    private final WriteBehindBuffer writeBehindBuffer;
    private final BatchWriteService batchWriteService;

    @Scheduled(fixedRate = 2000)
    public void flushWriteBehindBuffer() {
        List<AliasWriteTask> batch = writeBehindBuffer.drain();
        if (!batch.isEmpty()) {
            try {
                batchWriteService.batchSave(batch);
                log.debug("Flushed {} aliases to database", batch.size());
            } catch (Exception e) {
                log.error("Failed to flush write-behind buffer: {}", e.getMessage(), e);
            }
        }
    }
}


