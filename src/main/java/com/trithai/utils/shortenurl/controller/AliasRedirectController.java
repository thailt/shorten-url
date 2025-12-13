package com.trithai.utils.shortenurl.controller;

import com.trithai.utils.shortenurl.service.ShortenUrlService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping()
public class AliasRedirectController {

    private final ShortenUrlService shortenUrlService;

    @GetMapping("/{alias}")
    public void shorten(HttpServletResponse response, @PathVariable String alias) {
        try {
            var aliasResponse = shortenUrlService.getAlias(alias);
            response.sendRedirect(aliasResponse.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
