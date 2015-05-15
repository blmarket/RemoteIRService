package com.example.blmarket.irservice.service;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.util.JsonReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created by blmarket on 2015-04-23.
 */
public class MyHttpServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final ConsumerIrManager service;

    public MyHttpServiceHandler(ConsumerIrManager service) {
        this.service = service;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        try {
            if (request.method().equals(HttpMethod.POST) == false) {
                throw new IllegalArgumentException("POST only please");
            }


            Integer freq = null;
            List<Integer> codes = null;

            JsonReader reader = new JsonReader(new InputStreamReader(new ByteBufInputStream(request.content())));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "freq":
                        freq = reader.nextInt();
                        break;
                    case "code":
                        codes = new ArrayList<Integer>();
                        reader.beginArray();
                        while (reader.hasNext()) {
                            codes.add(reader.nextInt());
                        }
                        reader.endArray();
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected key " + name);
                }
            }
            reader.endObject();
            reader.close();

            if (freq == null || codes == null) throw new IllegalArgumentException("bad format json");

            int[] codeArray = new int[codes.size()];
            int ptr = 0;
            for (Integer code : codes) {
                codeArray[ptr++] = code.intValue();
            }
            service.transmit(freq, codeArray);

            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("Hello World".getBytes()));
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(e.getMessage().getBytes()));
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelReadComplete(ctx);
    }
}
