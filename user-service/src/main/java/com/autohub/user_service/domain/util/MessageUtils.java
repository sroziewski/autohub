package com.autohub.user_service.domain.util;

import org.springframework.context.MessageSource;

public class MessageUtils {
    public static MessageSource messageSource;

    public static String getMessage(String key) {
        return messageSource.getMessage(key, null, null);
    }

    public static String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, null);
    }
}
