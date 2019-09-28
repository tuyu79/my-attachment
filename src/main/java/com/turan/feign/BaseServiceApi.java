package com.turan.feign;

import com.turan.common.RespCode;
import com.turan.common.ResultBean;
import com.turan.feign.api.baseservice.BaseServiceFeignApi;
import com.turan.feign.api.baseservice.bo.InstructionRequest;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "my-base-service",fallbackFactory = BaseServiceApiFallbackFactory.class)
public interface BaseServiceApi extends BaseServiceFeignApi
{

}

@Slf4j
class BaseServiceApiFallbackFactory implements FallbackFactory<BaseServiceApi>
{
    @Override
    public BaseServiceApi create(Throwable throwable)
    {
        return new BaseServiceApi()
        {
            @Override
            public ResultBean<Void> instruction(InstructionRequest request)
            {
                log.error("[api error],base service: ",throwable);
                return ResultBean.of(RespCode.SYS_ERROR);
            }
        };
    }
}
