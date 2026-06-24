package com.cc.springaiagent.controller;

import com.cc.springaiagent.constant.AiConstant;
import com.cc.springaiagent.entity.DocumentTask;
import com.cc.springaiagent.mq.DocumentProcessProducer;
import com.cc.springaiagent.service.IDocumentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 知识库文档管理控制器
 * <p>
 * 提供文档上传、状态查询、任务列表、删除等 REST 接口。
 * 文档上传后通过 RabbitMQ 异步处理（分块→向量化→ES索引）。
 * <p>
 * 异步处理流程：
 * <pre>
 * 用户上传 → PENDING → [RabbitMQ] → PROCESSING → COMPLETED
 *                                     ↘ FAILED → DLQ（死信队列）
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/document")
@Tag(name = "知识库文档管理", description = "文档上传、处理状态查询、任务列表、删除")
public class DocumentController {

    @Resource
    private IDocumentTaskService documentTaskService;

    @Resource
    private DocumentProcessProducer documentProcessProducer;

    /**
     * 上传文档并触发异步处理
     *
     * @param file 上传的文件（支持 md / txt / json / pdf）
     * @return 任务信息（含 taskId，前端凭此轮询状态）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文档", description = "上传知识库文档，系统将异步进行分块、向量化和ES索引")
    public Map<String, Object> uploadDocument(
            @Parameter(description = "文档文件，支持 md / txt / json / pdf") @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();

        // 1. 校验文件
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String fileType = getFileType(originalFilename);

        // 2. 生成任务ID和存储路径
        String taskId = UUID.randomUUID().toString();
        String saveDir = AiConstant.FILE_SAVE_DIR + "/documents";
        String savedFileName = taskId + "_" + originalFilename;
        Path savePath = Path.of(saveDir, savedFileName);

        try {
            // 3. 确保目录存在
            Files.createDirectories(Path.of(saveDir));

            // 4. 保存文件到磁盘
            file.transferTo(savePath.toFile());
            log.info("文件已保存: {}", savePath);

            // 5. 写入数据库（状态：PENDING）
            DocumentTask task = new DocumentTask();
            task.setTaskId(taskId);
            task.setFileName(originalFilename);
            task.setFileType(fileType);
            task.setFilePath(savePath.toString());
            task.setFileSize(file.getSize());
            task.setStatus(DocumentTask.STATUS_PENDING);
            documentTaskService.save(task);

            // 6. 投递到 RabbitMQ 异步处理
            documentProcessProducer.sendDocumentProcessTask(taskId);

            result.put("success", true);
            result.put("message", "文档已上传，正在排队处理");
            result.put("taskId", taskId);
            result.put("fileName", originalFilename);
            result.put("fileSize", file.getSize());
            result.put("status", DocumentTask.STATUS_PENDING);
            log.info("文档上传成功: taskId={}, fileName={}", taskId, originalFilename);

        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "文件保存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询文档处理状态
     *
     * @param taskId 任务ID（上传时返回）
     * @return 任务详情
     */
    @GetMapping("/status/{taskId}")
    @Operation(summary = "查询处理状态", description = "根据任务ID查询文档处理进度")
    public Map<String, Object> getStatus(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();

        DocumentTask task = documentTaskService.getByTaskId(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }

        result.put("success", true);
        result.put("taskId", task.getTaskId());
        result.put("fileName", task.getFileName());
        result.put("fileType", task.getFileType());
        result.put("fileSize", task.getFileSize());
        result.put("status", task.getStatus());
        result.put("chunkCount", task.getChunkCount());
        result.put("errorMsg", task.getErrorMsg());
        result.put("createTime", task.getCreateTime());
        result.put("updateTime", task.getUpdateTime());
        return result;
    }

    /**
     * 查询所有文档任务列表（按创建时间倒序）
     *
     * @return 任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "文档任务列表", description = "查询所有文档处理任务，按创建时间倒序排列")
    public Map<String, Object> listTasks() {
        Map<String, Object> result = new HashMap<>();
        List<DocumentTask> tasks = documentTaskService.listAllOrderByTime();

        List<Map<String, Object>> taskList = tasks.stream().map(task -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskId", task.getTaskId());
            item.put("fileName", task.getFileName());
            item.put("fileType", task.getFileType());
            item.put("fileSize", task.getFileSize());
            item.put("status", task.getStatus());
            item.put("chunkCount", task.getChunkCount());
            item.put("errorMsg", task.getErrorMsg());
            item.put("createTime", task.getCreateTime());
            item.put("updateTime", task.getUpdateTime());
            return item;
        }).toList();

        result.put("success", true);
        result.put("total", taskList.size());
        result.put("data", taskList);
        return result;
    }

    /**
     * 删除文档任务（同时清理ES中的索引数据）
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除文档", description = "删除文档任务记录及已索引的数据")
    public Map<String, Object> deleteTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();

        DocumentTask task = documentTaskService.getByTaskId(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }

        // 删除磁盘文件
        try {
            Path filePath = Path.of(task.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("已删除文件: {}", filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", e.getMessage());
        }

        // 删除数据库记录
        documentTaskService.removeByTaskId(taskId);

        result.put("success", true);
        result.put("message", "文档已删除");
        log.info("文档任务已删除: taskId={}", taskId);
        return result;
    }

    /**
     * 根据文件扩展名判断文件类型
     */
    private String getFileType(String filename) {
        if (filename == null) return "txt";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".md")) return "md";
        if (lower.endsWith(".txt")) return "txt";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".pdf")) return "pdf";
        return "txt";
    }
}
