package com.example.blmarket.irservice.service;

import android.hardware.ConsumerIrManager;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Created by blmarket on 2015-04-23.
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private final ConsumerIrManager service;

    public HttpServerInitializer(ConsumerIrManager service) {
        this.service = service;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new MyHttpServiceHandler(service));
    }
}
