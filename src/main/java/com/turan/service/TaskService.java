package com.turan.service;

import com.alibaba.fastjson.JSON;
import com.turan.common.BizException;
import com.turan.common.IRespCode;
import com.turan.common.RespCode;
import com.turan.config.AttachmentConfig;
import com.turan.dao.AttachmentTaskDAO;
import com.turan.enums.AttachmentTaskStatus;
import com.turan.example.mq.AttachmentUpload;
import com.turan.feign.BaseServiceApi;
import com.turan.feign.api.attachment.bo.TaskRequest;
import com.turan.feign.api.baseservice.bo.InstructionRequest;
import com.turan.po.AttachmentTask;
import com.turan.util.ResultHandleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TaskService
{
    @Autowired
    private AttachmentTaskDAO taskDAO;
    @Autowired
    private SequenceGenerator sequenceGenerator;
    @Autowired
    private BaseServiceApi baseServiceApi;
    @Autowired
    private AttachmentConfig attachmentConfig;

    public IRespCode send9208(TaskRequest request)
    {
        AttachmentTask attachmentTask = new AttachmentTask();
        attachmentTask.setTaskNo(getTaskNo());
        attachmentTask.setVehicleNo(request.getVehicleNo());
        attachmentTask.setPlatformAlarmUid(request.getPlatformAlarmUid());
        if (request.getFileCount() != null)
        {
            attachmentTask.setFileCount(request.getFileCount());
        }
        attachmentTask.setStatus(AttachmentTaskStatus.WAITING_UPLOAD.getCode());
        attachmentTask.setCreateAt(new Date());
        attachmentTask.setUpdateAt(new Date());

        taskDAO.insert(attachmentTask);

        AttachmentUpload attachmentUpload = new AttachmentUpload();
        attachmentUpload.setAddr(attachmentConfig.getAddr());
        attachmentUpload.setTcpPort(attachmentConfig.getPort());
        attachmentUpload.setUdpPort(-1);
        attachmentUpload.setAlarmMarkUid(request.getAlarmMarkUid());
        attachmentUpload.setPlatformAlarmUid(request.getPlatformAlarmUid() + "");

        InstructionRequest instructionRequest = new InstructionRequest();
        instructionRequest.setVehicleNo(request.getVehicleNo());
        instructionRequest.setMsgType("UPLOAD_ATTACHMENT");
        instructionRequest.setData(JSON.toJSONString(attachmentUpload));

        ResultHandleUtil.onFail(baseServiceApi.instruction(instructionRequest), () ->
        {
            log.error("[send 9208 fail],request: [{}]", instructionRequest);
            throw new BizException(RespCode.SYS_ERROR);
        });

        log.info("[send 9208 success],request : [{}] ", instructionRequest);

        return RespCode.SUCCESS;
    }

    public Long getTaskNo()
    {
        // todo turan 唯一id生成
        return sequenceGenerator.nextId();
    }
}
