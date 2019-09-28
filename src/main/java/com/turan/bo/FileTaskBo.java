package com.turan.bo;

import com.turan.example.protocol.item.FileDataPacket;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description TODO
 * @Date 2019/6/1 22:21
 * @Created by turan
 */
@Data
public class FileTaskBo
{
    private Long fileTaskNo;
    private String platformAlarmUid;
    private String filename;
    private int fileType;
    private long totalSize;
    private AtomicLong receivedSize = new AtomicLong(0);
    private List<FileDataPacket> completedPackets = new ArrayList<>();
}
