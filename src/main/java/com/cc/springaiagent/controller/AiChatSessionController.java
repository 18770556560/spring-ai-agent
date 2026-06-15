package com.cc.springaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * AI对话会话主表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-06-03
 */
@RestController
@RequestMapping("/aiChatSession")
public class AiChatSessionController {
    @GetMapping("/get")
    public String get() {
        return "successful to get aiChatSession";
    }
}
