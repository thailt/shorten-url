package com.trithai.utils.shortenurl.service.impl;

import com.trithai.utils.shortenurl.config.AppConfig;
import com.trithai.utils.shortenurl.dto.AliasCreateRequest;
import com.trithai.utils.shortenurl.dto.AliasCreateResponse;
import com.trithai.utils.shortenurl.entity.Alias;
import com.trithai.utils.shortenurl.exceptions.AliasNotFoundException;
import com.trithai.utils.shortenurl.repository.AliasRepository;
import com.trithai.utils.shortenurl.service.KeyGenerationService;
import com.trithai.utils.shortenurl.service.ShortenUrlService;
import com.trithai.utils.shortenurl.utils.LRUCache;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Primary
@Qualifier("hash-map-shorten-service")
public class ShortenUrlServiceHashMapBaseImpl implements ShortenUrlService {

    private final LRUCache<String, AliasCreateResponse> shortenUrlMap = new LRUCache<>(5);

    private final KeyGenerationService keyGenerationService;
    private final AppConfig appConfig;
    private final AliasRepository aliasRepository;

    @Override
    public AliasCreateResponse getAlias(String shortUrl) {
        if (shortenUrlMap.containsKey(shortUrl)) {
            return shortenUrlMap.get(shortUrl);
        }
        var alias = aliasRepository.findAliasByAlias(shortUrl);
        if (alias != null) {
            var aliasRes =  AliasCreateResponse.builder()
                    .url(alias.getUrl())
                    .alias(alias.getAlias())
                    .build();

            shortenUrlMap.put(shortUrl, aliasRes);
            return aliasRes;
        }

        throw new AliasNotFoundException(shortUrl);
    }

    @Override
    public AliasCreateResponse createShortUrl(AliasCreateRequest longUrl) {
        String key = keyGenerationService.createUniqueKey();
        var response =
                AliasCreateResponse.builder()
                        .alias(key)
                        .expire(longUrl.getExpire())
                        .url(longUrl.getUrl())
                        .shortenUrl(appConfig.getShortenDomain() + "/" + key)
                        .build();
        shortenUrlMap.put(key, response);
        aliasRepository.save(Alias.builder().alias(key).url(longUrl.getUrl()).build());
        return response;
    }
}
