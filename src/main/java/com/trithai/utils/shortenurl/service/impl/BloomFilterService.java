package com.trithai.utils.shortenurl.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BloomFilterService  {

    private final RedissonClient redissonClient;
    private RBloomFilter<String> shortenBloomFilter;


    public BloomFilterService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        shortenBloomFilter = getShortenBloomFilter();
    }

    @Synchronized
    public RBloomFilter<String> getShortenBloomFilter() {

        if (Objects.isNull(shortenBloomFilter)) {
            RBloomFilter<String> stringRBloomFilter = redissonClient
                    .getBloomFilter("bf-alias");
            stringRBloomFilter.tryInit(99999, 0.001);
            shortenBloomFilter = stringRBloomFilter;
        }

        return shortenBloomFilter;
    }

    public boolean checkShortenURLAvailability(String shortenURL) {
        return getShortenBloomFilter().contains(shortenURL);
    }

    public void addData(List<String> data){
        getShortenBloomFilter().add(data);
    }
    public long count(){
        return getShortenBloomFilter().count();
    }
}
