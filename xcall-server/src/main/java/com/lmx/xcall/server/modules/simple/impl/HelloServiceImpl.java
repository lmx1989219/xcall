package com.lmx.xcall.server.modules.simple.impl;

import com.lmx.xcall.server.core.RpcService;
import com.lmx.xcall.server.modules.simple.HelloService;

// 指定远程接口
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

	@Override
	public String hello(String name) {
		return "Hello! " + name;
	}

}
