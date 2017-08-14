package simple;


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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-client.xml", "classpath:rpc-config.xml"})
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;
    @Autowired
    HelloService helloService;
    ExecutorService es = Executors.newFixedThreadPool(8);

    @Test
    public void helloTest() {
        try {
            es.submit(new Runnable() {
                @Override
                public void run() {
//                    HelloService helloService = rpcProxy.create(HelloService.class);
                    for (int i = 0; i < 10; i++) {
                        String result = helloService.hello("World");
                        Assert.assertEquals("Hello! World", result);
                    }
                }
            });
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getLocalIp() throws UnknownHostException {
        System.out.println("ip:" + InetAddresses.toAddrString(InetAddress.getLocalHost()));
    }

}
