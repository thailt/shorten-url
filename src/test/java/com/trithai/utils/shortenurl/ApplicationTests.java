package com.trithai.utils.shortenurl;

import org.junit.jupiter.api.Test;
import com.trithai.utils.shortenurl.service.impl.BloomFilterService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private BloomFilterService bloomFilterService;

    @Test
    void contextLoads() {}
}
