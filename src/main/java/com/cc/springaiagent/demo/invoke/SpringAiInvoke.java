package com.cc.springaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class SpringAiInvoke implements CommandLineRunner {
    @Resource
    ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
        String response = dashscopeChatModel.call("你是什么模型");
        System.out.println(response);
    }
}
