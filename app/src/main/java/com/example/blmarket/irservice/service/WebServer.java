package com.example.blmarket.irservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

    /*
    private class WebDaemon extends NanoHTTPD {
        private final ConsumerIrManager service = (ConsumerIrManager) getApplicationContext().getSystemService(Context.CONSUMER_IR_SERVICE);

        public WebDaemon() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();
            if (method != method.POST) {
                IOUtils.closeQuietly(session.getInputStream());
                return new Response(Response.Status.BAD_REQUEST, "text/plain", "POST only please");
            }

            byte[] arr = new byte[102476];

            try {
                IOUtils.readFully(session.getInputStream(), arr);
                System.out.println(ByteBuffer.wrap(arr).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            JsonReader reader;
            Integer freq = null;
            ArrayList<Integer> codes = null;
            try {
                reader = new JsonReader(new InputStreamReader(session.getInputStream()));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    System.out.println(name);
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
                    }
                }
                reader.endObject();
                reader.close();
            } catch (Exception ex) {
                Log.e("WebServer", "json read error", ex);
            }

            System.out.println(freq + " " + codes);

            if (freq == null || codes == null) {
                return new Response(Response.Status.BAD_REQUEST, "application/json", "Invalid code");
            }

            int[] codeArray = new int[codes.size()];
            int ptr = 0;
            for (Integer code : codes) {
                codeArray[ptr++] = code.intValue();
            }

            service.transmit(freq, codeArray);
            System.out.println("Sending OK");
            return new Response(Response.Status.OK, "application/json", "\"OK\"");
        }
    }*/

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
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            Channel ch = bootstrap.bind(PORT).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        return START_STICKY;
    }
}
