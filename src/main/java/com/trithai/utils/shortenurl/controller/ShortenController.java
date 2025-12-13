package com.trithai.utils.shortenurl.controller;

import com.trithai.utils.shortenurl.dto.AliasCreateRequest;
import com.trithai.utils.shortenurl.dto.AliasCreateResponse;
import com.trithai.utils.shortenurl.service.ShortenUrlService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping("/app/api")
public class ShortenController {

    private final ShortenUrlService shortenUrlService;

    @PostMapping("/create")
    public AliasCreateResponse shorten(@Valid @RequestBody AliasCreateRequest alias) {
        return shortenUrlService.createShortUrl(alias);
    }
}
