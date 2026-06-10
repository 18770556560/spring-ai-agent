package com.mcpserver.aimcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;
    @Test
    void searchImage() {
        String s = imageSearchTool.searchImage("羽毛球",new ToolContext(Map.of("page","1","per_page","5")));
        System.out.println(s);
        Assertions.assertNotNull( s);
    }
}