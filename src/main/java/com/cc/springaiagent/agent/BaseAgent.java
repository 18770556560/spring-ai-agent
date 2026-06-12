package com.cc.springaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.cc.springaiagent.constant.AgentStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.LinkedList;
import java.util.List;


/**
 * 定义智能体运行流程
 */
@Data
@Slf4j
abstract class BaseAgent {
    //智能体名称
    private String name;
    //系统提示词
    private String systemPrompt;
    //下一步提示词
    private String nextStepPrompt;
    //大模型客户端
    private ChatClient chatClient;
    //智能体状态
    private AgentStatus status = AgentStatus.IDLE;
    //当前步骤
    private int currentStep = 0;
    //最大步骤
    private int maxStep = 4;
    //循环判断数
    private int loopCount = 2;
    //上下文消息
    private List<Message> messageList=new LinkedList<>();

    public String run(String userInput) {
        if (StrUtil.isBlank(userInput)) {
            throw new RuntimeException("can't be empty with userInput");
        }
        if (status != AgentStatus.IDLE) {
            throw new RuntimeException("can't run from current status:" + status);
        }

        status = AgentStatus.RUNNING;
        List<String> results = new LinkedList<>();
        messageList.add(new UserMessage(userInput));
        try {
            while (currentStep < maxStep && status != AgentStatus.COMPLETED) {
                currentStep += 1;
                String result = step();
                log.info("current step:{}/{},result:{}\n", currentStep, maxStep, result);

                results.add("step: " + currentStep + " , result: " + result);
            }
            if(this.currentStep>=this.maxStep){
                results.add("达到最大步骤限制");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            log.error("agent-run运行异常:{}", e.getMessage());
            status = AgentStatus.ERROR;
            return String.format("Run error:【%s】", e.getMessage());
        } finally {
            clean();
        }
    }

    /**
     * 执行步骤方法，子类必须实现
     *
     * @return
     */
    abstract String step();

    protected void clean() {
        //子类可以重写方法
    }
}
