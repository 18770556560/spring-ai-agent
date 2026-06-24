package com.cc.springaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.springaiagent.entity.DocumentTask;
import com.cc.springaiagent.mapper.mysql.DocumentTaskMapper;
import com.cc.springaiagent.service.IDocumentTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 文档处理任务 服务实现类
 * </p>
 *
 * @author CC
 * @since 2026-06-24
 */
@Service
public class DocumentTaskServiceImpl extends ServiceImpl<DocumentTaskMapper, DocumentTask> implements IDocumentTaskService {

    @Override
    public DocumentTask getByTaskId(String taskId) {
        LambdaQueryWrapper<DocumentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTask::getTaskId, taskId);
        return getOne(wrapper);
    }

    @Override
    public List<DocumentTask> listAllOrderByTime() {
        LambdaQueryWrapper<DocumentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DocumentTask::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean removeByTaskId(String taskId) {
        LambdaQueryWrapper<DocumentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTask::getTaskId, taskId);
        return remove(wrapper);
    }
}
