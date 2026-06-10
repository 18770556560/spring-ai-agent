package com.cc.springaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * AI对话消息明细表
 * </p>
 *
 * @author Baomidou
 * @since 2026-06-03
 */
@Data
@TableName("ai_chat_message")
@Schema(description = "AI对话消息明细表，存储单轮问答记录")
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "消息主键ID")
    private Long id;

    /**
     * 关联会话ID
     */
    @Schema(description = "关联会话唯一ID，绑定ai_chat_session表")
    private String sessionId;

    /**
     * 消息类型 1用户提问USER 2AI回复ASSISTANT 3系统提示词SYSTEM
     */
    @Schema(description = "消息类型：1=用户提问，2=AI回复，3=系统提示词")
    private int msgType;

    /**
     * 消息正文内容
     */
    @Schema(description = "消息具体文本内容")
    private String content;

    /**
     * 本条消息消耗token数量
     */
    @Schema(description = "本条内容消耗的Token数目")
    private Integer tokenNum;

    /**
     * 消息发送时间
     */
    @TableField(fill = FieldFill.INSERT) // 仅插入时填充
    @Schema(description = "消息创建时间")
    private LocalDateTime createTime;

    /**
     * 逻辑删除 0未删 1已删
     */
    @Schema(description = "逻辑删除：false正常，true已删除")
    private Boolean isDeleted;

}