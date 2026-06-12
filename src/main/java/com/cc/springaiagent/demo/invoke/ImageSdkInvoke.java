package com.cc.springaiagent.demo.invoke;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;

public class ImageSdkInvoke {

    // 以下为华北2（北京）地域的URL，各地域的URL不同。
    static {Constants.baseHttpApiUrl="https://dashscope.aliyuncs.com/api/v1";}
    @Value("${spring.ai.dashscope.api-key}")
    private static String apiKey;
//    private static final String apiKey = "apikey";
//    private static final String model = "apikey";

    public static void simpleMultiModalConversationCall()
            throws ApiException, NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", "D:\\AppGallery\\Downloads\\IntelliJ IDEA 2023.2.3\\IdeaProjects\\spring-ai-agent\\src\\main\\resources\\images\\watermelon.jpg"),
                        Collections.singletonMap("text", "解释图片?"))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apiKey(apiKey)
                .model("qwen3.7-plus")  // 此处以qwen3.7-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                .messages(Arrays.asList(userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);
        System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
    }
    public static void main(String[] args) {
        try {
            simpleMultiModalConversationCall();
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}