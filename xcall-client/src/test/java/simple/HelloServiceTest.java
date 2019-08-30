package simple;


import com.google.common.net.InetAddresses;
import com.lmx.xcall.server.modules.simple.EchoService;
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
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-client.xml"/*, "classpath:rpc-config.xml"*/})
public class HelloServiceTest {

    @Autowired
    HelloService helloService;
    //    @Autowired
//    EchoService echoService;
    ExecutorService es = Executors.newFixedThreadPool(8);

    @Test
    public void helloTest() {
        try {
            for (int i = 0; i < 10; i++) {
                es.submit(() -> {
                    try {
                        Assert.assertEquals("Hello! World", helloService.hello("World"));
//                            Assert.assertEquals("echo! 0", echoService.echo("0"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            es.shutdown();
            es.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getLocalIp() {
        try {
            System.out.println("ip:" + InetAddresses.toAddrString(InetAddress.getLocalHost()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
