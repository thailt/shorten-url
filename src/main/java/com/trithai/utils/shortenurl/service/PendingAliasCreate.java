package com.trithai.utils.shortenurl.service;

import com.trithai.utils.shortenurl.entity.Alias;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public record PendingAliasCreate(
        String alias,
        String url,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        CompletableFuture<Alias> result) {}
