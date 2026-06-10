package com.cc.springaiagent.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import com.alibaba.cloud.ai.toolcalling.common.interfaces.SearchService;
import com.alibaba.cloud.ai.toolcalling.metaso.MetasoService;
import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import com.cc.springaiagent.constant.AiConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AiTools {

    private final MetasoService metasoService;
    private final WeatherService weatherService;
    private final BaiduSearchService baiduSearchService;

    // 注入 Metaso 服务
    public AiTools(MetasoService metasoService, WeatherService weatherService, BaiduSearchService baiduSearchService) {
        this.metasoService = metasoService;
        this.weatherService = weatherService;
        this.baiduSearchService = baiduSearchService;
    }


//    @Tool(description = "read content from a file")
    public String writeFile(
            @ToolParam(description = "name of the file") String fileName
            , @ToolParam(description = "cotent of the file") String content) {
        String filePath = AiConstant.FILE_SAVE_DIR + "/" + fileName;

        try {
            FileUtil.mkdir(AiConstant.FILE_SAVE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "successful to write the file" + filePath;
        } catch (Exception e) {
            return "failed to write the file:" + e.getMessage();
        }
    }


//    @Tool(description = "read content from a file")
    public String readFile(@ToolParam(description = "name of the file") String fileName) {
        String filePath = AiConstant.FILE_SAVE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "failed to read the file:" + e.getMessage();
        }
    }

    /**
     * 秘塔搜索
     */
    /*@Tool(name = "webSearch",description = "search for information from Metaso search engine")
    public String webSearch(@ToolParam(description = "key words of search query") String query) {
        try {
            MetasoService.Response response = metasoService.apply(MetasoService.Request.simplyQuery(query));
            StringBuilder sb = new StringBuilder();
            response.getSearchResult().results().forEach(result -> {
                sb.append("标题：").append(result.title()).append("\n");
                sb.append("链接：").append(result.url()).append("\n");
                sb.append("内容：").append(result.content()).append("\n\n");
            });
            return sb.toString();
        } catch (Exception e) {
            log.error("metaso搜索失败: ", e.getMessage());
            return "搜索失败：" + e.getMessage();
        }
    }*/

    /**
     * 百度搜索
     */
    @Tool(name = "baiduSearch",description = "search for information from baidu search engine")
    public String baiduSearch(@ToolParam(description = "key words of search query") String query) {
        try {
            StringBuilder sb = new StringBuilder();
            SearchService.Response res = baiduSearchService.query(query);
            res.getSearchResult().results().forEach(result -> {
                sb.append("标题：").append(result.title()).append("\n");
                sb.append("链接：").append(result.url()).append("\n");
                sb.append("内容：").append(result.content()).append("\n\n");
            });
            return sb.toString();
        } catch (Exception e) {
            log.info("百度搜索失败: ", e.getMessage());
            return "百度搜索失败：" + e.getMessage();
        }
    }

    @Tool(name = "weatherSearch",description = "search for weather")
    public WeatherService.Response weatherSearch(@ToolParam(description = "THE CITY OF INQUIRY") String city,@ToolParam(description = "The number of days for which the weather is forecasted",required = false) int day) {
        try {
            WeatherService.Response apply = weatherService.apply(new WeatherService.Request(city, day));
            return apply ;
        } catch (Exception e) {
            log.info("weatherSearch error: {}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "Generate a PDF file with given content",returnDirect = true)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = AiConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsunb.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(
//                        fontPath,
//                        PdfEncodings.IDENTITY_H, // 关键：支持中文的编码
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED// 关键：把字体嵌入PDF，避免乱码
//                );
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }


}
