package com.sendByOP.expedition.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Profile("prod")
public class WebSecurityConfig implements WebMvcConfigurer {

    private final EncryptionInterceptor encryptionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(encryptionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
    }
    
    /**
     * Configure HttpFirewall to prevent path traversal attacks
     */
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // Reject URLs containing characters that could be used in path traversal
        firewall.setAllowSemicolon(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedPercent(false);
        firewall.setAllowUrlEncodedPeriod(false);
        return firewall;
    }
}