package com.lmx.xcall.client;

import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import com.lmx.xcall.common.RpcRequest;
import com.lmx.xcall.common.RpcResponse;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class RpcProxy {
    private long checkPeroid = 3000;
    private ServiceDiscovery serviceDiscovery;
    private RpcClientPool clientPool = new RpcClientPool();
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private Thread checkActive = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (clientPool.getConnPool().size() == 0)
                        continue;
                    for (Map.Entry<String, BlockingQueue<RpcClient>> entry : clientPool.getConnPool().entrySet()) {
                        Iterator<RpcClient> clients = entry.getValue().iterator();
                        while (clients.hasNext()) {
                            RpcRequest request = new RpcRequest();
                            request.setRequestId("ping");
                            RpcClient client = null;
                            try {
                                client = clients.next();
                                client.sendOnly(request);
                                LOGGER.debug("conn:{} is ok", client);
                            } catch (Exception e) {
                                LOGGER.error("{} conn error", client, e);
                                clientPool.removeConn(entry.getKey(), client);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    try {
                        Thread.sleep(checkPeroid);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        checkActive.start();
        ServiceDiscovery.eventBus.register(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        String uniqueKey = request.getClassName();
                        LOGGER.debug("cur thread {} invoke {}", Thread.currentThread().getId(), uniqueKey);
                        RpcClient client = clientPool.getConn(uniqueKey);
                        if (client != null) {
                            try {
                                RpcResponse response = client.sendAndGet(request);
                                if (response.isError()) {
                                    throw response.getError();
                                } else {
                                    return response.getResult();
                                }
                            } finally {
                                clientPool.releaseConn(uniqueKey, client);
                            }
                        } else {
                            LOGGER.warn("no provider for service {}", uniqueKey);
                        }

                        return null;
                    }
                });
    }

    @Subscribe
    private void subService(ServiceDiscovery.EventObj eventObj) {
        for (String add : eventObj.data) {
            HostAndPort hostAndPort = HostAndPort.fromString(add);
            String host = hostAndPort.getHostText();
            int port = hostAndPort.getPort();
            String uniqueKey = eventObj.serviceName;
            LOGGER.info("subscribe service {} success on remote host {}:{}", uniqueKey, host, port);
            clientPool.init(uniqueKey, host, port);
        }
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }
}