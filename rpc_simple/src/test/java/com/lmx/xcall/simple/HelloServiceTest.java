package com.lmx.xcall.simple;


import com.google.common.net.InetAddresses;
import com.lmx.xcall.client.RpcProxy;
import com.lmx.xcall.server.modules.simple.HelloService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-client.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest() {
        try {
            for (int i = 0; i < 100; i++) {
                HelloService helloService = rpcProxy.create(HelloService.class);
                String result = helloService.hello("World");
                Assert.assertEquals("Hello! World", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getLocalIp() throws UnknownHostException {
        System.out.println("ip:" + InetAddresses.toAddrString(InetAddress.getLocalHost()));
    }

}
