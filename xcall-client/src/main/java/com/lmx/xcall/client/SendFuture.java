package com.lmx.xcall.client;

import com.lmx.xcall.common.RpcResponse;

import java.util.concurrent.*;

/**
 * Created by limingxin on 2017/8/13.
 */
public class SendFuture implements Future<RpcResponse> {
    CountDownLatch cd;
    String requestId;
    RpcResponse response;

    public CountDownLatch getCd() {
        return cd;
    }

    public void setCd(CountDownLatch cd) {
        this.cd = cd;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        cd.countDown();
        return false;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        return this.response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!cd.await(timeout, unit)) {
            throw new TimeoutException();
        }
        return this.response;
    }
}
