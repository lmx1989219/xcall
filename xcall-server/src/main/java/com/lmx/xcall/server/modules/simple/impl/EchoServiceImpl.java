package com.lmx.xcall.server.modules.simple.impl;

import com.lmx.xcall.server.core.RpcService;
import com.lmx.xcall.server.modules.simple.EchoService;

// 指定远程接口
@RpcService(EchoService.class)
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String name) {
        return "echo! " + name;
    }

}
