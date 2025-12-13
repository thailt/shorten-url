package com.trithai.utils.shortenurl.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppConfig {
    private final String shortenDomain;
    private final Long machineId;

    public AppConfig(
            @Value("${app.shorten-domain}") String shortenDomain,
            @Value("${app.machine-id}") Long machineId) {
        this.shortenDomain = shortenDomain;
        this.machineId = machineId;
    }
}
