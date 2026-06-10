package com.cc.springaiagent.service.impl;

import com.cc.springaiagent.entity.AiChatSession;
import com.cc.springaiagent.mapper.mysql.AiChatSessionMapper;
import com.cc.springaiagent.service.IAiChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI对话会话主表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-06-03
 */
@Service
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService {

}
