package com.ganlansi.chat.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 配置类
 */
@RefreshScope
@Component
@Data
public class MyProperties {
    @Value("${openai_api_key}")
    private String openaiApiKey;

    @Value("${https_proxy}")
    private String httpsProxy;

    @Value("${openai_api_model}")
    private String openaiApiModel;



}
