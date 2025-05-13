package com.autohub.user_service.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

public class LocaleHeaderFilter extends OncePerRequestFilter {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("pl");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String acceptLanguage = request.getHeader("Accept-Language");
        Locale locale = DEFAULT_LOCALE;
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            Locale tempLocale = Locale.forLanguageTag(acceptLanguage.trim());
            locale = Locale.of(tempLocale.getLanguage());
        }

        LocaleContextHolder.setLocale(locale);
        try {
            filterChain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
