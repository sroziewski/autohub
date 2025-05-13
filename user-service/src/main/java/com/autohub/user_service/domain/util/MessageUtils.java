package com.autohub.user_service.domain.util;

import org.springframework.context.MessageSource;

import java.util.Locale;

public class MessageUtils {
    public static MessageSource messageSource;

    public static String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
