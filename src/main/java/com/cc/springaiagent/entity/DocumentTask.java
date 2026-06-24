package com.cc.springaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文档处理任务表 — 记录异步文档处理的完整生命周期
 * </p>
 * <p>
 * 流程：用户上传 → PENDING → 发送到 RabbitMQ → PROCESSING → COMPLETED / FAILED
 * </p>
 *
 * @author CC
 * @since 2026-06-24
 */
@Data
@TableName("document_task")
@Schema(description = "文档处理任务表，跟踪文档上传→分块→向量化→ES索引的全流程")
public class DocumentTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "自增主键")
    private Long id;

    /**
     * 全局唯一任务ID（UUID），暴露给前端查询状态
     */
    @Schema(description = "任务唯一编号，前端通过此ID轮询处理状态")
    private String taskId;

    /**
     * 原始文件名
     */
    @Schema(description = "用户上传的原始文件名")
    private String fileName;

    /**
     * 文件类型：md / txt / pdf / json
     */
    @Schema(description = "文件类型：md、txt、pdf、json")
    private String fileType;

    /**
     * 文件在服务器上的存储路径
     */
    @Schema(description = "文件在服务器上的存储路径")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小，单位：字节")
    private Long fileSize;

    /**
     * 处理状态：PENDING → PROCESSING → COMPLETED / FAILED
     */
    @Schema(description = "处理状态：PENDING=待处理, PROCESSING=处理中, COMPLETED=已完成, FAILED=处理失败")
    private String status;

    /**
     * 文档分块数量（处理完成后回填）
     */
    @Schema(description = "文档被拆分成的块数量")
    private Integer chunkCount;

    /**
     * 已重试次数（消费者处理失败时递增，用于判断是否超限）
     */
    @Schema(description = "消费重试次数，超过上限后进入死信队列")
    private Integer retryCount;

    /**
     * 失败原因（仅 FAILED 状态时有值）
     */
    @Schema(description = "处理失败时的错误详情")
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "任务创建时间")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "任务最后更新时间（状态变更时自动刷新）")
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================

    /** 待处理 — 文件已保存，消息已投递到 RabbitMQ */
    public static final String STATUS_PENDING = "PENDING";
    /** 处理中 — 消费者已取出消息，正在分块/向量化/索引 */
    public static final String STATUS_PROCESSING = "PROCESSING";
    /** 已完成 — 文档已成功索引到 Elasticsearch */
    public static final String STATUS_COMPLETED = "COMPLETED";
    /** 处理失败 — 重试耗尽后进入死信队列 */
    public static final String STATUS_FAILED = "FAILED";
}
