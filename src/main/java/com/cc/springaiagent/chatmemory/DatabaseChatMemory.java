package com.cc.springaiagent.chatmemory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.springaiagent.entity.AiChatMessage;
import com.cc.springaiagent.entity.AiChatSession;
import com.cc.springaiagent.service.IAiChatMessageService;
import com.cc.springaiagent.service.IAiChatSessionService;
import com.esotericsoftware.kryo.Kryo;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于文件持久化的对话记忆实现类。
 * <p>
 * 该类实现了 {@link ChatMemory} 接口，使用 Kryo 序列化框架将对话消息持久化存储到本地文件系统中。
 * 每个会话（Conversation）对应一个独立的二进制文件，文件名由会话ID决定。
 */
@Slf4j
@Component
public class DatabaseChatMemory implements ChatMemory {
    @Autowired
    private IAiChatSessionService aiChatSessionService;
    @Autowired
    private IAiChatMessageService aiChatMessageService;

    /**
     * Kryo 序列化实例，线程不安全，但在本例中主要用于单线程或同步场景下的简单演示
     */
    private static final Kryo kryo = new Kryo();

    static {
        // 配置 Kryo 不需要预先注册类，以便支持动态类型的序列化
        kryo.setRegistrationRequired(false);
        // 设置实例化策略，允许在没有默认构造函数的情况下创建对象实例
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 向指定会话中添加消息。
     * <p>
     * 注意：当前实现为空，需补充具体逻辑（通常包括读取现有消息、追加新消息、然后保存）。
     *
     * @param sessionId 会话唯一标识符
     * @param messages       要添加的消息列表
     */
    @Override
    @Transactional
    public void add(String sessionId, List<Message> messages) {
        //先判断是否是首次会话，首次会话需要创建会话数据
        AiChatSession aiChatSession = aiChatSessionService.getOne(new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getSessionId, sessionId));
        if (aiChatSession == null){
            aiChatSession = new AiChatSession();
            aiChatSession.setSessionId(sessionId);
            aiChatSession.setUserId("1");
            aiChatSession.setModelName("qwen-plus");
            aiChatSession.setSessionTitle(MessageType.USER.equals(messages.get(0).getMessageType())? messages.get(0).getText():("用户提问"+LocalDateTime.now()));
            aiChatSessionService.save(aiChatSession);
        }else{
            aiChatSessionService.updateById(aiChatSession);
        }
        // 2.SpringAI消息 → 数据库消息实体

        List<AiChatMessage> dbMsgList = messages.stream().<AiChatMessage>map(message -> {
            AiChatMessage dbMsg = new AiChatMessage();
            dbMsg.setSessionId(sessionId);
            dbMsg.setContent(message.getText());
            dbMsg.setMsgType(switch (message.getMessageType()) {
                case MessageType.USER -> 1;
                case MessageType.ASSISTANT -> 2;
                case MessageType.SYSTEM -> 3;
                default -> throw new RuntimeException("不支持的消息");
            });
            return dbMsg;
        }).toList();
        aiChatMessageService.saveBatch(dbMsgList);
    }

    /**
     * 获取指定会话的所有历史消息。
     * <p>
     * 注意：当前实现返回 null，需补充具体逻辑（通常调用 getOrCreateConversation 方法）。
     *
     * @param sessionId 会话唯一标识符
     * @return 该会话的历史消息列表
     */
    @Override
    public List<Message> get(String sessionId) {
        List<AiChatMessage> messageList = aiChatMessageService.list(new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getSessionId, sessionId).orderByAsc(AiChatMessage::getId));
        List<Message> list = messageList.stream().<Message>map(dbMsg -> switch (dbMsg.getMsgType()) {
            case 1 -> new UserMessage(dbMsg.getContent());
            case 2 -> new AssistantMessage(dbMsg.getContent());
            case 3 -> new SystemMessage(dbMsg.getContent());
            default -> throw new RuntimeException("未知消息类型");
        }).toList();
        return list;
    }

    /**
     * 清除指定会话的所有历史消息。
     * <p>
     * 注意：当前实现为空，需补充具体逻辑（通常包括删除对应的持久化文件）。
     *
     * @param sessionId 会话唯一标识符
     */
    @Override
    public void clear(String sessionId) {
        //清除会话记录
        aiChatSessionService.remove(new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getSessionId, sessionId));
        //清除消息记录
        aiChatMessageService.remove(new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getSessionId, sessionId));
    }
}
