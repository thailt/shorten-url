package com.trithai.utils.shortenurl.exceptions;

import lombok.Getter;

@Getter
public class AliasNotFoundException extends RuntimeException {
    private final String alias;

    public AliasNotFoundException(String alias) {
        super("Alias '" + alias + "' not found");
        this.alias = alias;
    }
}
