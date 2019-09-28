package com.turan.util;

import com.turan.common.RespCode;
import com.turan.common.ResultBean;

public class ResultHandleUtil
{
    public static <T> void onFail(ResultBean<T> result, FailCallback failCallback)
    {
        if (result.getCode() != RespCode.SUCCESS.getCode())
        {
            failCallback.callback();
        }
    }

    public interface FailCallback
    {
        void callback();
    }
}

