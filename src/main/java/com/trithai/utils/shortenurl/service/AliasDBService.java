package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.exceptions.CreatingAliasExistedException;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AliasDBService {
    private final AliasRepository aliasRepository;

    @Transactional
    public Alias getFromDb(String shortUrl) {
        return aliasRepository.findAliasByAlias(shortUrl);
    }

    @Transactional
    public Alias saveToDb(String key, String url, LocalDateTime expiredAt) {
        try {
            return aliasRepository.save(
                    Alias.builder()
                            .alias(key)
                            .createdAt(LocalDateTime.now())
                            .expiredAt(expiredAt)
                            .url(url)
                            .build());
        } catch (DataIntegrityViolationException ex) {
            throw new CreatingAliasExistedException(key);
        }
    }
}
