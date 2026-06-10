package com.cc.springaiagent.advisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    // 标记：只有设置了这个参数的请求，才会打印日志
    public static final String ENABLE_LOGGING = "enable_logging";
    private final int order;

    ObjectMapper mapper = new ObjectMapper();

    public MyLoggerAdvisor() {
        this.order = 99999;
    }

    public MyLoggerAdvisor(int order) {
        this.order = order;
    }

    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    protected void logRequest(ChatClientRequest request){
        // 拿到本轮所有消息
        List<Message> messages = request.prompt().getInstructions();
        // 遍历找最后一条用户消息（用户最新提问）
        String userMsg = messages.stream()
                .filter(m -> m instanceof UserMessage)
                .map(Message::getText)
                .reduce((first, last) -> last)
                .orElse("无用户消息");

        log.info("用户提问：{}", userMsg);
        try {
            String contextJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request.context());
            log.info("完整上下文：{}", contextJson);
        } catch (JsonProcessingException e) {
            log.info("完整上下文：加载失败");
        }
    }

    protected void logResponse(ChatClientResponse chatClientResponse) {
        log.info("qwen response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return this.order;
    }

    public String toString() {
        return MyLoggerAdvisor.class.getSimpleName();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int order = 0;

        private Builder() {
        }



        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public MyLoggerAdvisor build() {
            return new MyLoggerAdvisor( this.order);
        }
    }
}
