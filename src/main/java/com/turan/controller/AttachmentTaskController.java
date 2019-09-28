package com.turan.controller;

import com.turan.common.ResultBean;
import com.turan.feign.api.attachment.AttachmentFeignApi;
import com.turan.feign.api.attachment.bo.TaskRequest;
import com.turan.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AttachmentTaskController implements AttachmentFeignApi
{

    @Autowired
    private TaskService taskService;

    @Override
    public ResultBean<Void> addTask(TaskRequest taskRequest)
    {
        return ResultBean.of(taskService.send9208(taskRequest));
    }
}
