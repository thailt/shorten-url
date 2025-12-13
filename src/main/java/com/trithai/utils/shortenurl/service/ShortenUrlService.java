package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.dto.AliasCreateRequest;
import com.trithai.utils.shortenurl.dto.AliasCreateResponse;

// Hash Map cache base
public interface ShortenUrlService {
    AliasCreateResponse getAlias(String shortUrl);

    AliasCreateResponse createShortUrl(AliasCreateRequest longUrl);
}
