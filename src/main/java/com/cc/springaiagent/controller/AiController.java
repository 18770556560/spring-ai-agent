package com.cc.springaiagent.controller;

import com.cc.springaiagent.agent.CcManus;
import com.cc.springaiagent.app.ForLove;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ForLove forLove;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel chatModel;

    /**
     * 同步调用 AI 恋爱大师应用，一次性输出结果
     */
    @GetMapping("/for_love/chat/sync")
    public String doChatLocalRagSync(String message, String chatId) {
        return forLove.doChatWithLocalRag(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用(如果不对响应进行控制，推荐使用这种方式)
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE相当于将响应头中的Content-Type设置为text/event-stream
     * 告诉前端调用接口时传Accept的值需要为text/event-stream
     */
    @GetMapping(value = "/for_love/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatLocalRagSSE(String message, String chatId) {
//        return forLove.doChatLocalRagStream(message, chatId);
        return forLove.doChatStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     */
    @GetMapping(value = "/for_love/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatLocalRagServerSentEvent(String message, String chatId) {
        return forLove.doChatLocalRagStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/for_love/chat/sse_emitter")
    public SseEmitter doChatLocalRagServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        forLove.doChatLocalRagStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message 用户输入
     * @return 结果
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        CcManus CcManus = new CcManus(allTools, chatModel);
        return CcManus.runStream(message);
    }
}
