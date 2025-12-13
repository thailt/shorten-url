package com.trithai.utils.shortenurl.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.trithai.utils.shortenurl.service.KeyGenerationService;
import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class KeyGenerationServiceImpl implements KeyGenerationService {
    public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

    public static final char[] DEFAULT_ALPHABET =
            "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    public static final int DEFAULT_SIZE = 10;

    @Override
    public String createUniqueKey() {
        return randomString();
    }

    private String randomString() {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE);
    }
}
