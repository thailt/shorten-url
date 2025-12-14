package com.trithai.utils.shortenurl.service.impl;

import static com.relops.snowflake.Snowflake.MAX_NODE;

import com.relops.snowflake.Snowflake;
import com.trithai.utils.shortenurl.config.AppConfig;
import com.trithai.utils.shortenurl.service.KeyGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Qualifier("snowflake-id-key-gen")
@Service
@Primary
@Slf4j
public class SnowflakeKeyGen implements KeyGenerationService {

    private static final char[] ALPHABET =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int BASE = ALPHABET.length;

    private final Snowflake snowflake;

    public SnowflakeKeyGen(AppConfig appConfig) {
        snowflake = new Snowflake((int) (appConfig.getMachineId() % MAX_NODE));
    }

    @Override
    public String createUniqueKey() {
        long nextId = snowflake.next();
        return encode(nextId);
    }

    public static String encode(long value) {
        if (value == 0) {
            return String.valueOf(ALPHABET[0]);
        }

        // Use a StringBuilder for efficient string concatenation
        StringBuilder str = new StringBuilder();
        long num = value;

        while (num > 0) {
            long remainder = num % BASE;
            num = num / BASE;
            // Prepend the character corresponding to the remainder
            str.insert(0, ALPHABET[(int) remainder]);
        }

        return str.toString();
    }
}
