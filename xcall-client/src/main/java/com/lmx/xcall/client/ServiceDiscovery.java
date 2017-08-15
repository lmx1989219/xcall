package com.lmx.xcall.client;

import com.google.common.eventbus.EventBus;
import com.lmx.xcall.common.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class ServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private Map<String, List<String>> dataList = new ConcurrentHashMap<>();

    private String registryAddress;

    public static EventBus eventBus = new EventBus();

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void subScribe(List<String> serviceNames) {
        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk, serviceNames);
        }
    }

    public List<String> discover(String serviceName) {
        int size = dataList.size();
        List<String> data = null;
        if (size > 0) {
            data = dataList.get(serviceName);
            LOGGER.debug("using only data: {}", data);
        }
        return data;
    }

    static public class EventObj {
        String serviceName;
        List<String> data;

        public EventObj(String serviceName, List<String> data) {
            this.serviceName = serviceName;
            this.data = data;
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk, final List<String> serviceNames) {
        try {
            for (final String serviceName : serviceNames) {
                List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH + "/" + serviceName, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                            watchNode(zk, serviceNames);
                        }
                    }
                });
                List<String> dataList = new ArrayList<>();
                for (String node : nodeList) {
                    byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + serviceName + "/" + node, false, null);
                    dataList.add(new String(bytes));
                }
                LOGGER.debug("node data: {}", dataList);
                this.dataList.put(serviceName, dataList);
                eventBus.post(new EventObj(serviceName, dataList));
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
