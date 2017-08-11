package com.lmx.xcall.client;

import com.lmx.xcall.common.Constant;
import io.netty.util.internal.ThreadLocalRandom;
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

    public ServiceDiscovery(String registryAddress, List<String> serviceNames) {
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk, serviceNames);
        }
    }

    public String discover(String serviceName) {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(serviceName).get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(serviceName).get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
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
                List<String> nodeList = zk.getChildren(Constant.ZK_DATA_PATH, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                            watchNode(zk, serviceNames);
                        }
                    }
                });
                List<String> dataList = new ArrayList<>();
                for (String node : nodeList) {
                    byte[] bytes = zk.getData(Constant.ZK_DATA_PATH + "/" + node, false, null);
                    dataList.add(new String(bytes));
                }
                LOGGER.debug("node data: {}", dataList);
                this.dataList.put(serviceName, dataList);
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
