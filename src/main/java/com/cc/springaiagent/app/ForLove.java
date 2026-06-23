package com.cc.springaiagent.app;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.cc.springaiagent.advisor.MyLoggerAdvisor;
import com.cc.springaiagent.utils.AiTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ForLove {
    // 方式1：使用构造器注入
    private final ChatClient chatClient;
    // 从类路径资源加载系统提示模板
    @Value("classpath:/prompts/system-message.st")
    private org.springframework.core.io.Resource systemResource;

    String systemPrompt = "你是污染防控智能问答助手，核心依托已入库的污染防控知识库进行回复。\n" +
            "1. 仅基于知识库内的问题、成因、应对措施、标准规范、问答内容响应提问，禁止拓展知识库以外的专业知识。\n" +
            "2. 用户提问对应到知识库某条内容时，完整提炼核心信息作答，逻辑清晰、重点突出。\n" +
            "3. 区分污染类型（大气、水、土壤、固废、噪声），精准匹配场景给出解决方案。\n" +
            "4. 遇到超出知识库范围的问题，统一回复：“当前暂无该问题的相关解答，请您咨询环保专业人员。”\n" +
            "5. 表述口语化、实操性强，避免晦涩专业术语堆砌，方便现场人员理解使用。\n";
//    String systemPrompt="你是恋爱心理顾问，先自我介绍，引导用户按【单身/恋爱/已婚】分类倾诉：\n" +
//        "1.单身：交友、追人难题\n" +
//        "2.恋爱：沟通、习惯矛盾\n" +
//        "3.已婚：家事、亲友矛盾\n" +
//        "让用户细说事件、对方反应、自身想法，定制方案。";
    /**
     * 初始化客户端
     *
     */
    public ForLove(ChatModel chatModel, ChatMemory databaseChatMemory) {
        //内部存储会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(3).build();
        //文件存储会话记忆
//        String fileDir=System.getProperty("user.dir")+"/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
//                .defaultTools()
                .defaultAdvisors(//默认顾问  每次对话时，会自动调用
                        MessageChatMemoryAdvisor.builder(databaseChatMemory).build(),//数据库存储会话记忆
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),//本地存储会话记忆
                        MyLoggerAdvisor.builder().build()
                        //自定义检查顾问
//                        ,new CheckAdvisor()
                        //重读
//                        ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 基础对话
     */
    public String doChat(String content,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(content)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call().chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 支持多轮对话记忆，SSE 流式传输,本地知识库
     */
    public Flux<String> doChatStream(String message, String chatId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
        return content;
    }

    /**
     * 基础对话
     *
     */
    public void imageChat(String content, String uri, String chatId) throws URISyntaxException, MalformedURLException {

        //1、参数map
        Map<String,Object> args = Map.of("name", "王老师");
        //2、渲染替换{name}
        // 直接使用资源创建模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        String sysContent = systemPromptTemplate.render(args);
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_PNG,
                new URI(uri).toURL().toURI()));

        UserMessage message =
                UserMessage.builder().text(content).media(mediaList).metadata(new HashMap<>()).build();
        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

        chatClient.prompt(new Prompt(message,
                        DashScopeChatOptions.builder().model("qwen3.7-plus").multiModel(true).build()))
                .system(sysContent)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call()
                .chatResponse();

    }

    /**
     * 结构化输出类
     * @param title
     * @param suggestions
     */
    record LoveReport(String title, List<String> suggestions) {
    }
    public LoveReport  doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(systemPrompt + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    //使用云知识库 顾问
//    @Resource
//    private Advisor loveAppRagCloudAdvisor;

    public String doChatWithCloudRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用增强检索服务（云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }


    /**
     * 使用内部存储的知识库
     */
    @Resource
    private VectorStore loveAppVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用知识库问答
                .advisors(QuestionAnswerAdvisor
                        //知识库内容读取/分割/存储
                        .builder(loveAppVectorStore)
                        // 配置SearchRequest：召回条数、相似度阈值
                        .searchRequest(SearchRequest.builder()
                                .topK(4) // 召回4条文档，自定义
                                .similarityThreshold(0.3d) // 相似度过滤
                                .build())
                        // 可选：自定义RAG提示模板，默认自带 {question_answer_context}占位符
                        // .userTextAdvise("根据上下文：{question_answer_context}回答用户问题：{query}")
                        .build())
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    //使用本地pg存储的知识库
    @Resource
    private VectorStore pgVectorVectorStore;


    @Resource
    RetrievalRerankAdvisor myRetrievalRerankAdvisor;

    public String doChatWithLocalRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(
                        myRetrievalRerankAdvisor
//                        ,QuestionAnswerAdvisor
//                                .builder(pgVectorVectorStore)
//                                // 配置SearchRequest：召回条数、相似度阈值
//                                .searchRequest(SearchRequest.builder()
//                                        .topK(10) // 召回4条文档，自定义
//                                        .similarityThreshold(0.3d) // 相似度过滤
//                                        .build())
//                                // 可选：自定义RAG提示模板，默认自带 {question_answer_context}占位符
//                                // .userTextAdvise("根据上下文：{question_answer_context}回答用户问题：{query}")
//                                .build()
                )
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 支持多轮对话记忆，SSE 流式传输,本地知识库
     */
    public Flux<String> doChatLocalRagStream(String message, String chatId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(
                        QuestionAnswerAdvisor
                                .builder(pgVectorVectorStore)
                                // 配置SearchRequest：召回条数、相似度阈值
                                .searchRequest(SearchRequest.builder()
                                        .topK(4) // 召回4条文档，自定义
                                        .similarityThreshold(0.3d) // 相似度过滤
                                        .build())
                                // 可选：自定义RAG提示模板，默认自带 {question_answer_context}占位符
                                // .userTextAdvise("根据上下文：{question_answer_context}回答用户问题：{query}")
                                .build()
                )
                .stream()
                .content();
        return content;
    }


    /**
     * 使用完整优化流程--rag模块化
     */
    @Resource
    private Advisor fullRetrievalAugmentationAdvisor;
    public String doChatWithfullRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(
                        fullRetrievalAugmentationAdvisor//查询重写、检索过滤、重排序、上下文增强
                        , myRetrievalRerankAdvisor
                )
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }


    /**
     * 使用工具
     */
    @Resource
    AiTools aiTools;
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .tools(aiTools)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    @Resource
    ToolCallbackProvider toolCallbackProvider;
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(toolCallbackProvider)//或者tool(toolCallbackProvider)
                //与mcp间传递参数，不会经过大模型  当前版本不生效
//                .toolContext(Map.of("page","1","per_page","5"))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    // ==================== ES 混合检索方法 ====================

    /**
     * 使用 ES 混合检索（向量 + BM25 文本 + RRF 融合）的全流程 RAG
     */
    @Resource
    private Advisor fullHybridRetrievalAugmentationAdvisor;

    /**
     * 同步：ES 混合检索 RAG
     */
    public String doChatWithHybridRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(fullHybridRetrievalAugmentationAdvisor)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 流式：ES 混合检索 RAG（SSE）
     */
    public Flux<String> doChatWithHybridRagStream(String message, String chatId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(fullHybridRetrievalAugmentationAdvisor)
                .stream()
                .content();
        return content;
    }

    // 注意：如需精排，取消下面注释并注入 hybridRetrievalRerankAdvisor
    // @Resource
    // private RetrievalRerankAdvisor hybridRetrievalRerankAdvisor;
    /**
     * 同步：ES 混合检索 RAG + 精排
     * <p>
     * 注意：需要取消 hybridRetrievalRerankAdvisor 字段的注释才能使用
     */
//    public String doChatWithHybridRagRerank(String message, String chatId) {
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(fullHybridRetrievalAugmentationAdvisor, hybridRetrievalRerankAdvisor)
//                .call()
//                .chatResponse();
//        return chatResponse.getResult().getOutput().getText();
//    }

}
