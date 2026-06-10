package com.cc.springaiagent.advisor;

import cn.hutool.core.util.StrUtil;
import com.cc.springaiagent.exception.SecurityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

@Slf4j
public class CheckAdvisor implements BaseAdvisor {

    //提示词校验过滤
    private String invalidString = "hcc";


    /**
     * Advisor 的执行顺序，数值越小优先级越高
     */
    private int order = 0;


    public CheckAdvisor() {
    }
    /**
     *
     * @param invalidString
     */
    public CheckAdvisor(String invalidString) {
        this.invalidString = invalidString;
    }

    /**
     * 在请求发送给 LLM 之前执行
     * <p>
     * 主要逻辑：
     * 1. 校验用户提示词中是否有违规词，如果存在则让ai
     *
     * @param chatClientRequest 当前的聊天请求
     * @param advisorChain      顾问链
     * @return 修改后的聊天请求
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 获取用户原始输入文本
        String originalUserText = chatClientRequest.prompt().getUserMessage().getText();

        // 忽略大小写
        boolean isInvalid = StrUtil.containsIgnoreCase(originalUserText, this.invalidString);
        if (isInvalid){
            log.info("用户输入包含违规词：{}", this.invalidString);
            // 抛出异常，中断后续流程
            throw new SecurityViolationException("输入内容包含违规词汇，请求已被拦截");
        }
        return chatClientRequest;
    }

    /**
     * 在收到 LLM 响应之后执行
     * <p>
     * 当前实现不做任何处理，直接返回原始响应。
     * 如果需要后处理（如解析、过滤），可以在此处添加逻辑。
     *
     * @param chatClientResponse 当前的聊天响应
     * @param advisorChain       顾问链
     * @return 原始聊天响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 获取执行顺序
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 设置执行顺序并返回当前实例（链式调用）
     *
     * @param order 新的顺序值
     * @return 当前 ReReadingAdvisor 实例
     */
    public CheckAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }
}
