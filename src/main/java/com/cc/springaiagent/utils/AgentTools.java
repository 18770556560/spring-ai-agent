package com.cc.springaiagent.utils;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class AgentTools {
    @Tool(description = "Used to tell the program to end the task")
    public String do_terminate() {
        return "任务完成";
    }
}
