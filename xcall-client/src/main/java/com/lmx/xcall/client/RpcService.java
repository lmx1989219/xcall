package com.lmx.xcall.client;

import com.lmx.xcall.client.springboot.RpcClient;
import com.lmx.xcall.server.modules.simple.HelloService;

/**
 * 声明一个远程bean定义
 *
 * @author: lucas
 * @create: 2019-08-30 10:10
 **/
@RpcClient(id = "helloService", value = HelloService.class)
public class RpcService {
}
