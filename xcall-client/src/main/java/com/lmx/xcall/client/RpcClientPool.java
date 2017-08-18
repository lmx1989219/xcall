package com.lmx.xcall.client;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by limingxin on 2017/8/16.
 */
public class RpcClientPool {
    private Map<String, BlockingQueue<RpcClient>> connPool = new ConcurrentHashMap<>();
    private final static long TIME_OUT = 5000;
    private int poolSize = 32;
    private int minPoolSize = 2;

    void init(String uniqueKey, String host, int port) {
        if (!connPool.containsKey(uniqueKey)) {
            connPool.put(uniqueKey, new ArrayBlockingQueue<RpcClient>(poolSize));
        }
        for (int i = 0; i < minPoolSize; i++) {
            RpcClient client = new RpcClient(host, port);
            client.initConn();
            if (connPool.get(uniqueKey).size() == poolSize)
                return;
            connPool.get(uniqueKey).add(client);
        }
    }

    RpcClient getConn(String uniqueKey) {
        try {
            return connPool.get(uniqueKey).poll(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    void releaseConn(String uniqueKey, RpcClient rpcClient) {
        connPool.get(uniqueKey).offer(rpcClient);
    }

    public Map<String, BlockingQueue<RpcClient>> getConnPool() {
        return connPool;
    }

    void removeConn(String uniqueKey, RpcClient rpcClient) {
        Iterator<RpcClient> it = connPool.get(uniqueKey).iterator();
        while (it.hasNext()) {
            RpcClient client = it.next();
            //same type(hashcode) client will be removed
            if (client.equals(rpcClient))
                it.remove();
        }
    }
}
