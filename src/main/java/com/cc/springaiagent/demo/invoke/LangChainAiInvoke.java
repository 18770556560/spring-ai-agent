package com.cc.springaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;


/**
 * @author 12428
 */
public class LangChainAiInvoke {
    @Value("${spring.ai.dashscope.api-key}")
    private static String apiKey;
    @Value("${spring.ai.dashscope.chat.options.model}")
    private static String model;
    public static void main(String[] args) {
        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .build();
        String response = qwenModel.chat("2023年的今天天气");
        System.out.println( response);

    }
}
