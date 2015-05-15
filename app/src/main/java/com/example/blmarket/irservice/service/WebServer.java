package com.example.blmarket.irservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by blmarket on 2015-04-19.
 */
public class WebServer extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final int PORT = 8080;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        InetAddress address;
        try {
            address = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(wifiManager.getConnectionInfo().getIpAddress()).array());
        } catch (UnknownHostException e) {
            Log.e("WebServer", "UnknownHostException", e);
            address = null;
        }

        Log.i("WebServer", "Starting service");

        Notification notification = new Notification.Builder(this)
                .setTicker("IR Service Ticker : " + address.getHostAddress()).build();

        startForeground(NOTIFICATION_ID, notification);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ConsumerIrManager service = (ConsumerIrManager) getApplicationContext().getSystemService(Context.CONSUMER_IR_SERVICE);
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpServerInitializer(service));

        bootstrap.bind(PORT);
        return START_STICKY;
    }
}
