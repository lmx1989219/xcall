package com.lmx.xcall.server.core;

import com.lmx.xcall.common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String serviceName, String data) {
        if (data != null) {
            ZooKeeper zk = connectServer(serviceName);
            if (zk != null) {
                createNode(zk, serviceName, data);
            }
        }
    }

    private ZooKeeper connectServer(String serviceName) {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            if (zk.exists(Constant.ZK_REGISTRY_PATH, true) == null)
                zk.create(Constant.ZK_REGISTRY_PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            if (zk.exists(Constant.ZK_REGISTRY_PATH + "/" + serviceName, true) == null)
                zk.create(Constant.ZK_REGISTRY_PATH + "/" + serviceName, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    private void createNode(ZooKeeper zk, String serviceName, String data) {
        try {
            byte[] bytes = data.getBytes();
            //FIXME must only build struct with tree
            zk.create(Constant.ZK_REGISTRY_PATH + "/" + serviceName + "/" + serviceName, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("service {} provider success on remote host {}", serviceName, data);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
