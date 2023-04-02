package com.ganlansi.chat.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//请求 放行  静态资源放行
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOriginPatterns("http?://*.*.com")
                .allowedMethods("GET", "HEAD", "POST","PUT", "DELETE", "OPTIONS")
                .allowCredentials(true).maxAge(3600);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //开放static,templates,public 目录 但是请求时候需要加上对应的前缀,比如我访问static下的资源/static/xxxx/xx.js
        registry.addResourceHandler("/static/**","/templates/**","/public/**")
                .addResourceLocations("classpath:/static/","classpath:/templates/","classpath:/public/");

    }

}
