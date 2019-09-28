package com.turan.enums;

import lombok.Getter;

@Getter
public enum FileTaskStatus
{
    INCOMPLETED(0,"未完成"),
    COMPLETED(1,"已完成")
    ;

    private int code;
    private String desc;

    FileTaskStatus(int code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }
}
