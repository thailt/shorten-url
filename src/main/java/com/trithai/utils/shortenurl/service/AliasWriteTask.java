package com.trithai.utils.shortenurl.service;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliasWriteTask {
    private String alias;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}


