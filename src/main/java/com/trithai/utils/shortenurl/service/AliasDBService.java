package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.dto.AliasCreateRequest;
import com.trithai.utils.shortenurl.dto.AliasCreateResponse;
import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AliasDBService {
    private final AliasRepository aliasRepository;

    @Transactional
    public Alias getFromDb(String shortUrl) {
        var alias = aliasRepository.findAliasByAlias(shortUrl);
        return alias;
    }

    @Transactional
    public Alias saveToDb(AliasCreateRequest longUrl, String key) {
        var alias =
                aliasRepository.save(
                        Alias.builder()
                                .alias(key)
                                .createdAt(LocalDateTime.now())
                                .expiredAt(
                                        Objects.requireNonNullElse(
                                                longUrl.getExpire(),
                                                LocalDateTime.now().plusYears(1)))
                                .url(longUrl.getUrl())
                                .build());
        return alias;
    }
}
