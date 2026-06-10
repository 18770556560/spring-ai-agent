package com.cc.springaiagent.app;

import cn.hutool.core.lang.UUID;
import com.cc.springaiagent.chatmemory.DatabaseChatMemory;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@SpringBootTest
class ForLoveTest {
    @Autowired
    ApplicationContext context;
    @Resource
    ForLove forLove;
    @Test
    void doChat() {
        String versionId = UUID.randomUUID().toString();
        versionId = "4ad6bb9f-2fa6-4f8a-874d-9bddfb689af5";
        String message1="我是谁，我开始和你说过";
        forLove.doChat(message1,versionId);
    }
    @Test
    void testBean(){
        //查看容器里有没有DatabaseChatMemory
        System.out.println(context.getBean(DatabaseChatMemory.class));
    }
    @Test
    void imageChat() throws URISyntaxException, MalformedURLException {
        String uri="https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";
        String message="请解释这个图片";

        forLove.imageChat(message, uri, java.util.UUID.randomUUID().toString());
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是大头图图，现在单身，我想让另一半（小美）更爱我，但我不知道该怎么做";
        ForLove.LoveReport loveReport = forLove.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我现在是恋情爱中，对方冷淡怎么办？对方心情不好的时候怎么办";
        String answer =  forLove.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithCloudRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是男性今年26岁，平时喜欢户外运动，能推荐个合适人选让我认识下吗";
        String answer =  forLove.doChatWithCloudRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithLocalRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是单身，我想谈恋爱";
        String answer =  forLove.doChatWithLocalRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithfullRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "脱单指南";
        message = "老人可以去运动吗";
//        message = "I'm single and looking for a relationship.\n";
        String answer =  forLove.doChatWithfullRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        String chatId = UUID.randomUUID().toString();
        String message = "北京天气怎么样，最近有什么相关新闻吗，将内容输出pdf报告发给我，内容分成天气和新闻两个模块";
        String answer =  forLove.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我找几张好看的小狗照片";
        String answer =  forLove.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}