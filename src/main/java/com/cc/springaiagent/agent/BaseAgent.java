package com.cc.springaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.cc.springaiagent.constant.AgentStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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
            if(this.currentStep>=this.maxStep && this.status!=AgentStatus.COMPLETED){
                results.add("达到最大步骤限制");
                this.status=AgentStatus.COMPLETED;
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
     * 流式输出
     * @param userInput 用户输入
     * @return
     */
    public SseEmitter runStream(String userInput) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            // 1、基础校验
            try {
                if (this.status != AgentStatus.IDLE) {
                    sseEmitter.send("错误：无法从状态运行代理：" + this.status);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userInput)) {
                    sseEmitter.send("错误：不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
            // 2、执行，更改状态
            this.status = AgentStatus.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userInput));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                while (currentStep < maxStep && status != AgentStatus.COMPLETED) {
                    currentStep += 1;
                    String result = step();
                    log.info("当前步骤:{}/{},执行结果:{}\n", currentStep, maxStep, result);
                    String stepResult = String.format("步骤: %d , 结果: %s", currentStep, result);

                    // 输出当前每一步的结果到 SSE
                    sseEmitter.send(stepResult);
                }

                // 检查是否超出步骤限制
                if(this.currentStep>=this.maxStep && this.status!=AgentStatus.COMPLETED){
                    this.status = AgentStatus.COMPLETED;
                    results.add("执行结束：达到最大步骤 (" + maxStep + ")");
                    sseEmitter.send("执行结束：达到最大步骤（" + maxStep + "）");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                this.status = AgentStatus.ERROR;
                log.error("智能体异常：", e);
                try {
                    sseEmitter.send("执行错误：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 3、清理资源
                this.clean();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.status = AgentStatus.ERROR;
            this.clean();
            log.warn("SSE 连接超时");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.status == AgentStatus.RUNNING) {
                this.status = AgentStatus.COMPLETED;
            }
            this.clean();
            log.info("SSE 传输完成");
        });
        return sseEmitter;
    }

    /**
     * 执行步骤方法，子类必须实现
     *
     * @return 步骤执行结果
     */
    abstract String step();

    protected void clean() {
        //子类可以重写方法
    }
}
