package com.trithai.utils.shortenurl.service.impl;

import com.trithai.utils.shortenurl.config.AppConfig;
import com.trithai.utils.shortenurl.dto.AliasCreateRequest;
import com.trithai.utils.shortenurl.dto.AliasCreateResponse;
import com.trithai.utils.shortenurl.exceptions.AliasNotFoundException;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import com.trithai.utils.shortenurl.service.AliasDBService;
import com.trithai.utils.shortenurl.service.KeyGenerationService;
import com.trithai.utils.shortenurl.service.ShortenUrlService;
import com.trithai.utils.shortenurl.utils.LRUCache;

import jakarta.annotation.PostConstruct;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Primary
@Qualifier("hash-map-shorten-service")
public class ShortenUrlServiceHImpl implements ShortenUrlService {

    private final LRUCache<String, AliasCreateResponse> shortenUrlMap = new LRUCache<>(1000);

    private final KeyGenerationService keyGenerationService;
    private final AppConfig appConfig;
    private final AliasRepository aliasRepository;
    private final RedisTemplate<String, AliasCreateResponse> aliasReponseRedisTemplate;

    private final BloomFilterService bloomFilterService;
    private final AliasDBService aliasDBService;

    @PostConstruct
    public void init() {
        long batchSize = 1_000_000L;

        long current = bloomFilterService.count();
        log.info("Bloom filter count: {}", current);

        for (long i = current; true; i = i + batchSize) {
            List<String> aliases = aliasRepository.findAllAlias(i, i + batchSize).stream().toList();
            if (aliases == null || aliases.isEmpty()) {
                break;
            }
            bloomFilterService.addData(aliases);
            log.info("Bloom Filter added {}", i + batchSize);
        }

    }

    @Override
    public AliasCreateResponse getAlias(String shortUrl) {
        if (shortenUrlMap.containsKey(shortUrl)) {
            return shortenUrlMap.get(shortUrl);
        }

        if (!bloomFilterService.checkShortenURLAvailability(shortUrl)) {
            log.info("Shortening url {} failed", shortUrl);
            throw new AliasNotFoundException(shortUrl);
        }

        var cachedResponse = aliasReponseRedisTemplate.opsForValue().get(shortUrl);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        var alias = aliasDBService.getFromDb(shortUrl);
        if (alias != null) {
            var aliasRes =
                    AliasCreateResponse.builder()
                            .url(alias.getUrl())
                            .alias(alias.getAlias())
                            .build();

            shortenUrlMap.put(shortUrl, aliasRes);
            aliasReponseRedisTemplate.opsForValue().set(shortUrl, aliasRes, Duration.ofDays(10));
            return aliasRes;
        }

        throw new AliasNotFoundException(shortUrl);
    }


    @Override
    public AliasCreateResponse createShortUrl(AliasCreateRequest longUrl) {
        String key = keyGenerationService.createUniqueKey();
        LocalDateTime expiredAt =
                longUrl.getExpire() != null
                        ? longUrl.getExpire()
                        : LocalDateTime.now().plusYears(1);

        var savedAlias = aliasDBService.saveToDb(key, longUrl.getUrl(), expiredAt);

        var response =
                AliasCreateResponse.builder()
                        .alias(savedAlias.getAlias())
                        .expire(savedAlias.getExpiredAt())
                        .url(savedAlias.getUrl())
                        .shortenUrl("http://" + appConfig.getShortenDomain() + "/" + savedAlias.getAlias())
                        .build();

        shortenUrlMap.put(savedAlias.getAlias(), response);
        bloomFilterService.addData(List.of(savedAlias.getAlias()));
        aliasReponseRedisTemplate
                .opsForValue()
                .set(savedAlias.getAlias(), response, Duration.ofDays(10));
        return response;
    }
}
