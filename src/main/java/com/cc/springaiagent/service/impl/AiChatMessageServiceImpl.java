package com.cc.springaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.springaiagent.entity.AiChatMessage;
import com.cc.springaiagent.mapper.mysql.AiChatMessageMapper;
import com.cc.springaiagent.service.IAiChatMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI对话消息明细表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-06-03
 */
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage> implements IAiChatMessageService {

}
