package com.mnq.logsfinder.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final boolean authInterceptorEnabled;


    @Autowired
    public WebConfig(AuthInterceptor authInterceptor, @Value("${app.auth.interceptor.enabled}")boolean authInterceptorEnabled) {
        this.authInterceptor = authInterceptor;
        this.authInterceptorEnabled = authInterceptorEnabled;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (authInterceptorEnabled) {
            registry.addInterceptor(authInterceptor);
        }
    }
}

