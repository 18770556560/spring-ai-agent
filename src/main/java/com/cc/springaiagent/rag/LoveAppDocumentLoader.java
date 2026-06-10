package com.cc.springaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    /**
     * Spring 资源模式解析器，用于根据路径模式（如 classpath:document/*.md）批量获取资源文件。
     */
    private final ResourcePatternResolver resourcePatternResolver;


    /**
     * 构造函数，注入资源模式解析器。
     *
     * @param resourcePatternResolver Spring 容器提供的资源模式解析器实例
     */
    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载并解析类路径下 {@code document/} 目录中的所有 Markdown 文件。
     * <p>
     * 解析配置说明：
     * <ul>
     *   <li>遇到水平线（---）时创建新的文档片段</li>
     *   <li>排除代码块内容</li>
     *   <li>排除引用块内容</li>
     *   <li>将文件名作为元数据附加到每个生成的文档中</li>
     * </ul>
     *
     * @return 解析后的 {@link Document} 列表；若发生 IO 异常，则记录错误日志并返回已成功加载的部分结果
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 获取 classpath:document/ 目录下所有 .md 文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
//            Resource[] resources = resourcePatternResolver.getResources("classpath:document/单身人员册.md");
            // 获取 classpath:document/ 目录下所有 .json 文件
//            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.json");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();

                // 构建 Markdown 解析配置
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        // 当遇到 Markdown 水平线（--- 或 ***）时，将其视为文档分割点，创建一个新的 Document 对象
                        .withHorizontalRuleCreateDocument(true)
                        // 是否包含代码块（）。设置为 false 可过滤掉技术代码片段，仅保留自然语言文本
                        .withIncludeCodeBlock(false)
                        // 是否包含引用块（> quote）。设置为 false 可过滤掉引用内容，专注于正文信息
                        .withIncludeBlockquote(false)
                        // 添加自定义元数据，将源文件名存入 Document 的 metadata 中，便于后续溯源或调试
                        .withAdditionalMetadata("filename", fileName)
                        .build();

                // 创建阅读器并执行解析
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                //创建JSON阅读器
                JsonReader jsonReader = new JsonReader(resource);

                allDocuments.addAll(markdownDocumentReader.get());
//                allDocuments.addAll(jsonReader.get());
            }
        } catch (IOException e) {
            log.error("Json 文档加载失败", e);
//            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}

