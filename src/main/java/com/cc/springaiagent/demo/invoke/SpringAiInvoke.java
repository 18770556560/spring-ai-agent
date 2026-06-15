package com.cc.springaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;

//@Component
public class SpringAiInvoke implements CommandLineRunner {
    @Resource
    ChatModel chatModel;


    @Override
    public void run(String... args) throws Exception {
        String response = chatModel.call("你是什么模型");
        System.out.println(response);
    }
}
