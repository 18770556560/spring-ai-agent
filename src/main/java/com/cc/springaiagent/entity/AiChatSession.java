package com.cc.springaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI对话会话主表
 * </p>
 *
 * @author Baomidou
 * @since 2026-06-03
 */
@Data
@TableName("ai_chat_session")
@Schema(description = "AI对话会话主表，一条会话关联多条聊天消息")
public class AiChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "会话自增主键")
    private Long id;

    /**
     * 全局唯一会话ID（LangChain/SpringAi Memory标识）
     */
    @Schema(description = "会话唯一编号，用作AI记忆区分标识")
    private String sessionId;

    /**
     * 用户ID，多用户区分会话
     */
    @Schema(description = "所属用户ID，多用户隔离会话")
    private String userId;

    /**
     * 对话标题（首条消息自动生成）
     */
    @Schema(description = "对话标题，由首条提问自动生成")
    private String sessionTitle;

    /**
     * 大模型名称：qwen-plus等
     */
    @Schema(description = "使用的大模型名称，如qwen-turbo、deepseek-r1")
    private String modelName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT) // 仅插入时填充
    @Schema(description = "会话创建时间")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充
    @Schema(description = "会话最后修改时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删1已删
     */
    @Schema(description = "逻辑删除标识：false正常，true已删除")
    private Boolean isDeleted;
}