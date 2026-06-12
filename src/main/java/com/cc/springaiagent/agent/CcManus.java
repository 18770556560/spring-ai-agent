package com.cc.springaiagent.agent;

import com.cc.springaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class CcManus extends ToolCallAgent{

    CcManus(ToolCallback[] allTools, ChatModel chatModel) {
        super(allTools);
        setChatClient(ChatClient
                .builder(chatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build());
        setName("CcManus");
        String SYSTEM_PROMPT = """
                你是 CcManus, 一个全能ai助手, 帮助用户解决任何出现的问题.
                你有各种工具可供调用，高效完成复杂请求.
                """;
        setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                基于用户需要，主动选择最合适的工具或工具组合.
                对于复杂问题，你可以拆解问题并使用不同工具逐步解决它.
                每次使用工具后，清晰地解释执行结果并给出建议下一步操作.
                如果你想在任何时刻停止交互或者用户没有提出明确的需求，请使用'do_terminate'工具/函数调用.
                """;
        setNextStepPrompt(NEXT_STEP_PROMPT);
    }

}
