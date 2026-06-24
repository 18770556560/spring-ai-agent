package com.cc.springaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cc.springaiagent.entity.DocumentTask;

import java.util.List;

/**
 * <p>
 * 文档处理任务 服务接口
 * </p>
 *
 * @author CC
 * @since 2026-06-24
 */
public interface IDocumentTaskService extends IService<DocumentTask> {

    /**
     * 根据任务ID查询
     */
    DocumentTask getByTaskId(String taskId);

    /**
     * 查询所有任务（按创建时间倒序）
     */
    List<DocumentTask> listAllOrderByTime();

    /**
     * 根据任务ID删除
     */
    boolean removeByTaskId(String taskId);
}
