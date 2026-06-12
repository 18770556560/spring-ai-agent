package com.cc.springaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.cc.springaiagent.constant.AgentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReactAgent {
    //在chatOpetions中禁止ai内部自动调用工具，从而实现程序控制调用
    private ChatOptions chatOptions;
    //工具注册库，通过注入即可获取
    private ToolCallback[] allTools;
    //用于处理ai的工具调用请求，从而执行工具  用于act方法中
    private ToolCallingManager toolCallingManager;
    //存储思考过程中ai的结果，其中包含了工具调用请求
    private ChatResponse toolCallResponse;


    ToolCallAgent(ToolCallback[] allTools) {
        super();
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.allTools = allTools;
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    @Override
    protected Boolean think() {
        try {
            //将下一步提示词添加入上下文中
            if (StrUtil.isNotBlank(getNextStepPrompt())) {
                UserMessage userMessage = new UserMessage(getNextStepPrompt());
                getMessageList().add(userMessage);
            }
            //根据用户上下文 思考是否需要执行动作
            Prompt prompt = new Prompt(new ArrayList<>(getMessageList()), this.chatOptions);
            this.toolCallResponse = getChatClient()
                    .prompt(prompt)
                    .toolCallbacks(allTools)
                    .call()
                    .chatResponse();
            //获取助手消息，然后可以进一步获取思考结果和工具请求
            AssistantMessage assistantMessage = this.getToolCallResponse().getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            String text = assistantMessage.getText();
            if(toolCalls.isEmpty()){
                //只有不调用工具时才记录，因为调用时会记录
                getMessageList().add(assistantMessage);
                log.info("Think result:{},无需调用工具", text);
                //不需要调用工具则
                return false;
            }else{
                String toolNames = toolCalls.stream()
                        .map(AssistantMessage.ToolCall::name).collect(Collectors.joining("、"));
                log.info("Think result:{};请求使用{}个工具：{}", assistantMessage.getText(),toolCalls.size(), toolNames);
                return true;
            }
        } catch (Exception e) {
            log.error("Think error:{}", e.getMessage());
            throw new RuntimeException(String.format("Think error:【%s】]", e.getMessage()));
        }
    }

    /**
     * 根据会话历史最后一段消息，执行工具调用
     * @return
     */
    @Override
    protected String act() {
        try {
            //根据ai工具调用请求执行调用
            ToolExecutionResult toolExecutionResult = this.toolCallingManager
                    .executeToolCalls(new Prompt(this.getMessageList()), this.toolCallResponse);
            //conversationHistory中包含了历史会话消息
            this.setMessageList(toolExecutionResult.conversationHistory());
            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult.conversationHistory().getLast();
            String executeToolsLog = toolResponseMessage.getResponses().stream()
                    .map(response -> String.format("调用工具【%s】====结果【%s】", response.name(), response.responseData()))
                    .collect(Collectors.joining("\n"));
            log.info(executeToolsLog);
            boolean ifTerminate = toolResponseMessage.getResponses().stream().anyMatch(response -> response.name().equals("do_terminate"));
            if(ifTerminate){
                setStatus(AgentStatus.COMPLETED);
            }

            //将消息列表最后一个作为结果返回
            return this.getMessageList().getLast().getText();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Act error:【%s】", e.getMessage()));
        }
    }
}
