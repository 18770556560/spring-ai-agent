package com.cc.springaiagent.utils;

import jakarta.annotation.Resource;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具手动注册类
 */
@Configuration
public class ToolRegistration {

    @Resource
    AgentTools agentTools;
    @Resource
    AiTools aiTools;


    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
                agentTools,
                aiTools
        );
    }
}
