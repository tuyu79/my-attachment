package com.turan.service;

import com.google.common.base.Joiner;
import com.turan.bo.AttachmentTaskBo;
import com.turan.bo.FileTaskBo;
import com.turan.config.AttachmentConfig;
import com.turan.example.protocol.message.dev.D_ByteData;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

//import org.apache.commons.io.FileUtils;

@RunWith(MockitoJUnitRunner.class)
public class DByteDataServiceTest
{

    @InjectMocks
    private DByteDataService byteDataService;
    @Spy
    private T808InstructionService instructionService;
    @Mock
    private AttachmentConfig attachmentConfig;

    @Test
    public void whenDByteDataComingThenFileTaskReceivedSizeIncrease() throws IOException
    {
        String TEST_BASE_PATH = System.getProperty("user.dir");

        String platformAlarmUuid = "99999";
        String fileName = "00_0_6401_1_" + platformAlarmUuid + ".jpg";

        AttachmentTaskBo attachmentTaskBo = new AttachmentTaskBo();
        attachmentTaskBo.setTerminalId("123");
        attachmentTaskBo.setPlatformAlarmUid(platformAlarmUuid);
        attachmentTaskBo.setUploadType(0x00);
        attachmentTaskBo.setTotalCount(1);
        attachmentTaskBo.setCompleteCount(new AtomicInteger(0));

        FileTaskBo fileTaskBo = new FileTaskBo();
        fileTaskBo.setPlatformAlarmUid(platformAlarmUuid);
        fileTaskBo.setFilename(fileName);
        fileTaskBo.setFileType(0x00);
        fileTaskBo.setTotalSize(3);
        fileTaskBo.setReceivedSize(new AtomicLong(0));
        fileTaskBo.setCompletedPackets(Lists.newArrayList());

        Map<String, FileTaskBo> map = new HashMap<>();
        map.put(fileName, fileTaskBo);

        attachmentTaskBo.setFileTaskTable(map);

        doReturn(attachmentTaskBo).when(instructionService).getTaskByPlatformAlarmUid(anyString());
        doReturn(TEST_BASE_PATH).when(attachmentConfig).getFileBasePath();

        D_ByteData d_byteData = new D_ByteData();
        d_byteData.setFilename(fileName);
        d_byteData.setOffset(0);
        d_byteData.setLen(3);
        d_byteData.setBody(new byte[]{0x01, 0x02, 0x03});

        byteDataService.handleByteData(d_byteData);

        assertThat(fileTaskBo.getReceivedSize().get(), is(3L));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        String path = Joiner.on(File.separator).join(TEST_BASE_PATH,
                dateFormat.format(new Date()), fileTaskBo.getPlatformAlarmUid(), d_byteData.getFilename());

        File file = new File(path);
        assertTrue(file.exists());

        File deleteDir = new File(TEST_BASE_PATH + File.separator + dateFormat.format(new Date()));
        FileUtils.deleteDirectory(deleteDir);
    }

}