package com.trithai.utils.shortenurl.exceptions;

import lombok.Getter;

@Getter
public class CreatingAliasExistedException extends RuntimeException {
    private final String alias;

    public CreatingAliasExistedException(String alias) {
        super("Alias '" + alias + "' taken");
        this.alias = alias;
    }
}
