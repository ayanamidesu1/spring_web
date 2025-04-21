package com.example.web_pixiv.config;

import jakarta.servlet.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;


@Configuration
public class NoCorsFilterConfig {
    @Bean
    public FilterRegistrationBean<Filter> disableAllCorsFilters() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {
                // 直接放行，不做任何 CORS 校验
                chain.doFilter(req, res);
            }
        });
        // 设置最高优先级，保证在任何 CorsFilter 之前
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
