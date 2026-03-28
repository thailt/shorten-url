package com.trithai.utils.shortenurl.service.impl;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloomFilterService {

    private final RBloomFilter<String> shortenBloomFilter;

    public BloomFilterService(RedissonClient redissonClient) {
        this.shortenBloomFilter = redissonClient.getBloomFilter("bf-alias");
        this.shortenBloomFilter.tryInit(100_000_000, 0.001);
    }

    public boolean checkShortenURLAvailability(String shortenURL) {
        return shortenBloomFilter.contains(shortenURL);
    }

    public void addData(List<String> data) {
        shortenBloomFilter.add(data);
    }

    public long count() {
        return shortenBloomFilter.count();
    }
}
