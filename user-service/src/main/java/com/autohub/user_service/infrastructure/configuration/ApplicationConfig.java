package com.autohub.user_service.infrastructure.configuration;

import com.autohub.user_service.domain.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@RequiredArgsConstructor
@Configuration
public class ApplicationConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final MessageSource messageSource;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        MessageUtils.messageSource = messageSource;

    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
