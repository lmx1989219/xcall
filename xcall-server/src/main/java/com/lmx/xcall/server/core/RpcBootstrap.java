package com.lmx.xcall.server.core;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcBootstrap {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("spring-server.xml");
	}
}
