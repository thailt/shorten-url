package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MicroBatchFlushHandler {

    private final AliasRepository aliasRepository;

    @Transactional
    public List<Alias> saveBatch(List<Alias> aliases) {
        return aliasRepository.saveAll(aliases);
    }
}
