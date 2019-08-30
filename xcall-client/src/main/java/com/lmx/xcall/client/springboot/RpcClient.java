package com.lmx.xcall.client.springboot;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类似feignClient
 *
 * @author: lucas
 * @create: 2019-08-30 09:51
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcClient {
    /**
     * 服务类定义
     *
     * @return
     */
    Class<?> value();

    /**
     * beanId
     *
     * @return
     */
    String id();
}
