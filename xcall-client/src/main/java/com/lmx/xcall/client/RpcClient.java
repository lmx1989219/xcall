package com.lmx.xcall.client;

import com.lmx.xcall.common.RpcDecoder;
import com.lmx.xcall.common.RpcEncoder;
import com.lmx.xcall.common.RpcRequest;
import com.lmx.xcall.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
    private String host;
    private int port;
    private Channel channel;
    private static final Map<String, SendFuture> SEND_FUTURE_MAP = new ConcurrentHashMap<>();
    private static final long MAX_WAIT = 10 * 1000;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void initConn() {
        EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline()
                        .addLast(new RpcEncoder(RpcRequest.class))
                        .addLast(new RpcDecoder(RpcResponse.class))
                        .addLast(RpcClient.this);
            }
        }).option(ChannelOption.TCP_NODELAY, true);

        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (response.getRequestId().equals("pong"))
            return;
        String seqNo = response.getRequestId();
        try {
            SendFuture future = SEND_FUTURE_MAP.get(seqNo);
            if (future != null) {
                future.setResponse(response);
                future.isDone();
            }
        } finally {
            SEND_FUTURE_MAP.remove(seqNo);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client cause exception", cause);
        ctx.close();
    }

    public RpcResponse sendAndGet(RpcRequest request) throws Exception {
        SendFuture future = new SendFuture();
        future.setCd(new CountDownLatch(1));
        SEND_FUTURE_MAP.put(request.getRequestId(), future);
        LOGGER.debug("xcall invoke begin,req is:{}", request);
        checkConn();
        channel.writeAndFlush(request);
        RpcResponse response = future.get(MAX_WAIT, TimeUnit.MILLISECONDS);
        if (response != null) {
            return response;
        }
        return null;
    }

    public void sendOnly(RpcRequest request) throws Exception {
        LOGGER.debug("heartbeat task,req:{}", request);
        checkConn();
        channel.writeAndFlush(request);
    }

    void checkConn() throws Exception {
        if (!channel.isOpen())
            throw new Exception("conn is close");
    }

    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return (host + ":" + port).hashCode();
    }

    @Override
    public String toString() {
        return "RpcClient{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}