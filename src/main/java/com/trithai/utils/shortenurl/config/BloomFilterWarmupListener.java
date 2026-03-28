package com.trithai.utils.shortenurl.config;

import com.trithai.utils.shortenurl.repository.AliasRepository;
import com.trithai.utils.shortenurl.service.BloomFilterReadiness;
import com.trithai.utils.shortenurl.service.impl.BloomFilterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterWarmupListener {

    private static final int BLOOM_INIT_BATCH_SIZE = 10_000;

    private final AliasRepository aliasRepository;
    private final BloomFilterService bloomFilterService;
    private final BloomFilterReadiness bloomFilterReadiness;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void initBloomFilter() {
        log.info("Starting async bloom filter warm-up...");
        long lastId = 0;
        long totalLoaded = 0;

        try {
            while (true) {
                List<Object[]> rows = aliasRepository.findAliasesWithIdAfterId(
                        lastId, PageRequest.of(0, BLOOM_INIT_BATCH_SIZE));
                if (rows.isEmpty()) {
                    break;
                }

                List<String> aliases = rows.stream()
                        .map(row -> (String) row[1])
                        .toList();
                bloomFilterService.addData(aliases);

                lastId = ((Number) rows.getLast()[0]).longValue();
                totalLoaded += aliases.size();
                log.info("Bloom filter loaded {} aliases, cursor at id={}", totalLoaded, lastId);
            }

            bloomFilterReadiness.markReady();
            log.info("Bloom filter warm-up complete: {} aliases loaded", totalLoaded);
        } catch (Exception e) {
            log.error("Bloom filter warm-up failed at id={}: {}", lastId, e.getMessage(), e);
        }
    }
}
