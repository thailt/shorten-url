package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class BatchWriteService {

    private final AliasRepository aliasRepository;

    @Transactional
    public void batchSave(List<AliasWriteTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        try {
            List<Alias> aliases =
                    tasks.stream()
                            .map(
                                    task ->
                                            Alias.builder()
                                                    .alias(task.getAlias())
                                                    .url(task.getUrl())
                                                    .createdAt(task.getCreatedAt())
                                                    .expiredAt(task.getExpiredAt())
                                                    .build())
                            .collect(Collectors.toList());

            aliasRepository.saveAll(aliases);
            log.info("Successfully batch saved {} aliases to database", aliases.size());
        } catch (Exception e) {
            log.error("Error batch saving aliases: {}", e.getMessage(), e);
            throw e;
        }
    }
}


