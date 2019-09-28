package com.turan.enums;

import lombok.Getter;

@Getter
public enum AttachmentTaskStatus
{
    WAITING_UPLOAD(0, "等待上传"),
    UPLOADING(1, "正在上传"),
    SUCCESS(2, "上传成功"),
    FAIL(3, "上传失败"),
    CANCELLED(4, "已取消")
    ;

    private int code;
    private String desc;

    AttachmentTaskStatus(int code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }
}
