package com.cc.springaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆实现类。
 * <p>
 * 该类实现了 {@link ChatMemory} 接口，使用 Kryo 序列化框架将对话消息持久化存储到本地文件系统中。
 * 每个会话（Conversation）对应一个独立的二进制文件，文件名由会话ID决定。
 */
public class FileBasedChatMemory implements ChatMemory {

    /**
     * 持久化文件的基础目录路径
     */
    private final String BASE_DIR;

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
     * 构造函数，初始化文件存储目录。
     *
     * @param dir 用于存储对话记忆文件的基础目录路径。如果目录不存在，将自动创建。
     */
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 向指定会话中添加消息。
     * <p>
     * 注意：当前实现为空，需补充具体逻辑（通常包括读取现有消息、追加新消息、然后保存）。
     *
     * @param conversationId 会话唯一标识符
     * @param messages       要添加的消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        //通过会话id查询已有会话信息列表
        List<Message> messageList = getOrCreateConversation(conversationId);
        //追加入列表中
        messageList.addAll(messages);
        //持久化入文件
        saveConversation(conversationId, messageList);
    }

    /**
     * 获取指定会话的所有历史消息。
     * <p>
     * 注意：当前实现返回 null，需补充具体逻辑（通常调用 getOrCreateConversation 方法）。
     *
     * @param conversationId 会话唯一标识符
     * @return 该会话的历史消息列表
     */
    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    /**
     * 清除指定会话的所有历史消息。
     * <p>
     * 注意：当前实现为空，需补充具体逻辑（通常包括删除对应的持久化文件）。
     *
     * @param conversationId 会话唯一标识符
     */
    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()){
            file.delete();
        }
    }

    /**
     * 获取指定会话ID的历史消息列表。
     * <p>
     * 如果对应的持久化文件存在，则从文件中反序列化读取消息列表；
     * 如果文件不存在或读取发生异常，则返回一个新的空列表。
     *
     * @param conversationId 会话唯一标识符，用于定位具体的持久化文件
     * @return 该会话对应的消息列表，若读取失败或文件不存在则返回空列表
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 将指定会话的消息列表持久化保存到文件中。
     * <p>
     * 使用 Kryo 将消息列表序列化为二进制数据并写入对应的文件。
     * 如果写入过程中发生 IO 异常，将打印堆栈信息。
     *
     * @param conversationId 会话唯一标识符，用于确定保存的文件名
     * @param messages       需要保存的消息列表
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据会话ID生成对应的持久化文件对象。
     * <p>
     * 文件命名规则为：{BASE_DIR}/{conversationId}.kryo
     *
     * @param conversationId 会话唯一标识符
     * @return 对应的 File 对象
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }


}
