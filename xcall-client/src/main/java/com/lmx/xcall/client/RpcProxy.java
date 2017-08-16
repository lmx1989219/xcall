package com.lmx.xcall.client;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import com.lmx.xcall.common.RpcRequest;
import com.lmx.xcall.common.RpcResponse;
import io.netty.util.internal.ConcurrentSet;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RpcProxy {
    private long checkPeroid = 3000;
    private List<String> serverAddress;
    private ServiceDiscovery serviceDiscovery;
    private Map<String, Set<RpcClient>> connPool = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private Thread checkActive = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (connPool.size() == 0)
                        continue;
                    for (Map.Entry<String, Set<RpcClient>> entry : connPool.entrySet()) {
                        Set<RpcClient> clients = entry.getValue();
                        for (RpcClient client : clients) {
                            RpcRequest request = new RpcRequest();
                            request.setRequestId("ping");
                            try {
                                client.sendOnly(request);
                                LOGGER.debug("conn:{} is ok", client);
                            } catch (Exception e) {
                                LOGGER.error("{} conn error", client, e);
                                removePool(entry.getKey(), client);
                            }
                        }
                    }
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
                        Set<RpcClient> clients = getConnPool(uniqueKey);
                        if (!CollectionUtils.isEmpty(clients)) {
                            List<RpcClient> clientList = Lists.newArrayList(clients.iterator());
                            RpcResponse response = clientList.get((int) System.currentTimeMillis() % clients.size())
                                    .send(request);
                            if (response.isError()) {
                                throw response.getError();
                            } else {
                                return response.getResult();
                            }
                        } else {
                            LOGGER.warn("no provider for service {}", uniqueKey);
                        }
                        return null;
                    }
                });
    }


    private Set<RpcClient> getConnPool(String uniqueKey) {
        if (serviceDiscovery != null) {
            serverAddress = serviceDiscovery.discover(uniqueKey);
        }
        if (CollectionUtils.isEmpty(serverAddress)) {
            return null;
        }
        return connPool.get(uniqueKey);
    }

    @Subscribe
    private void subService(ServiceDiscovery.EventObj eventObj) {
        for (String add : eventObj.data) {
            HostAndPort hostAndPort = HostAndPort.fromString(add);
            String host = hostAndPort.getHostText();
            int port = hostAndPort.getPort();
            RpcClient client = new RpcClient(host, port);
            String uniqueKey = eventObj.serviceName;
            if (!connPool.containsKey(uniqueKey)) {
                connPool.put(uniqueKey, new ConcurrentSet<RpcClient>());
            }
            if (!connPool.get(uniqueKey).contains(client)) {
                client.initConn();
                LOGGER.info("subscribe service {} success on remote host:{}", uniqueKey, client);
                //init a five number of conn
                connPool.get(uniqueKey).add(client);
                connPool.get(uniqueKey).add(client);
                connPool.get(uniqueKey).add(client);
                connPool.get(uniqueKey).add(client);
                connPool.get(uniqueKey).add(client);
            }
        }
    }


    public void removePool(String uniqueKey, RpcClient client) {
        LOGGER.debug("remove inactive conn {}", client);
        connPool.get(uniqueKey).remove(client);
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }
}