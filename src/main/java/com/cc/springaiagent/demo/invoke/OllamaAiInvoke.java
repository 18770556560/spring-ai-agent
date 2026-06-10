package com.cc.springaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 取消注释即可在 SpringBoot 项目启动时执行
//@Component
public class OllamaAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel ollamaChatModel;//需要引入依赖才能使用ollama

    @Override
    public void run(String... args) throws Exception {
        String response = ollamaChatModel.call("你是谁");
        System.out.println(response);
    }
}
