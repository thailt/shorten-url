package com.trithai.utils.shortenurl.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppConfig {
    private final String shortenDomain;

    public AppConfig(@Value("${app.shorten-domain}") String shortenDomain) {
        this.shortenDomain = shortenDomain;
    }
}
