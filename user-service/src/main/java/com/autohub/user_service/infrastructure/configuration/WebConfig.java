package com.autohub.user_service.infrastructure.configuration;

import com.autohub.user_service.infrastructure.filter.LocaleHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<LocaleHeaderFilter> localeHeaderFilter() {
        FilterRegistrationBean<LocaleHeaderFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LocaleHeaderFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
