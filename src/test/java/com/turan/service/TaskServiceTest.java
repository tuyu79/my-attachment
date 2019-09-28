package com.turan.service;

import com.turan.common.IRespCode;
import com.turan.common.RespCode;
import com.turan.common.ResultBean;
import com.turan.config.AttachmentConfig;
import com.turan.dao.AttachmentTaskDAO;
import com.turan.feign.BaseServiceApi;
import com.turan.feign.api.attachment.bo.TaskRequest;
import com.turan.feign.api.baseservice.bo.InstructionRequest;
import com.turan.po.AttachmentTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceTest
{
    @InjectMocks
    @Spy
    private TaskService taskService;
    @Mock
    private AttachmentTaskDAO taskDAO;
    @Mock
    private AttachmentConfig attachmentConfig;
    @Mock
    private BaseServiceApi baseServiceApi;

    @Test
    public void send9208()
    {
        doReturn(1L).when(taskService).getTaskNo();
        doReturn("my.example.domain").when(attachmentConfig).getAddr();
        doReturn(ResultBean.of(RespCode.SUCCESS)).when(baseServiceApi).instruction(any(InstructionRequest.class));

        TaskRequest request = new TaskRequest();
        request.setVehicleNo(123456L);
        request.setPlatformAlarmUid(654321L);
        request.setFileCount(1);

        IRespCode result = taskService.send9208(request);

        verify(taskDAO,times(1)).insert(any(AttachmentTask.class));
        verify(baseServiceApi,times(1)).instruction(any(InstructionRequest.class));
        assertThat(result,is(RespCode.SUCCESS));
    }
}