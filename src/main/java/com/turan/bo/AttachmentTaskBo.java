package com.turan.bo;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description TODO
 * @Date 2019/6/1 22:20
 * @Created by turan
 */
@Data
public class AttachmentTaskBo
{
    private Long taskNo;
    private String terminalId;
    private String platformAlarmUid;
    private int uploadType;
    private long totalCount;
    private AtomicInteger completeCount = new AtomicInteger(0);
    private Map<String /* 文件名 */, FileTaskBo /* 文件上传任务 */> fileTaskTable = new ConcurrentHashMap<>();
}
