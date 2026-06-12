package com.cc.springaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CcManusTest {
    @Resource
    CcManus ccManus;

    @Test
    void test() {
        String userInput = "北京天气怎么样，最近有什么相关新闻吗，整理为有结构层次的内容并生成pdf报告";
        String result = ccManus.run(userInput);
        Assertions.assertNotNull( result);
    }

}