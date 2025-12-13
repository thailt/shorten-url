package com.trithai.utils.shortenurl.exceptions;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AliasError {
    private String alias;
    private String errorCode;
    private String errorMessage;
    private Map<String, String> errorMessages;

    public static AliasError of(String alias, String errorCode, String errorMessage) {
        return new AliasError(alias, errorCode, errorMessage, null);
    }

    public static AliasError of(
            String alias,
            String errorCode,
            String errorMessage,
            Map<String, String> errorMessages) {
        return new AliasError(alias, errorCode, errorMessage, errorMessages);
    }

    public static AliasError aliasExisted(String alias) {
        return AliasError.of(alias, "alias_existed", "alias_existed");
    }

    public static AliasError aliasNotFound(String alias) {
        return AliasError.of(alias, "alias_not_found", "alias_not_found");
    }
}
