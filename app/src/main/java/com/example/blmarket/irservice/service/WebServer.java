package com.example.blmarket.irservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by blmarket on 2015-04-19.
 */
public class WebServer extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final int PORT = 8080;

    static class MyBinder extends Binder {
    }

    private class WebDaemon extends NanoHTTPD {
        private final ConsumerIrManager service = (ConsumerIrManager) getApplicationContext().getSystemService(Context.CONSUMER_IR_SERVICE);

        public WebDaemon() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();
            if (method != method.POST) {
                return new Response(Response.Status.BAD_REQUEST, "application/json", "POST only please");
            }

            JsonReader reader = null;
            Integer freq = null;
            ArrayList<Integer> codes = null;
            try {
                reader = new JsonReader(new BufferedReader(new InputStreamReader(session.getInputStream())));
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
    }

    private static final IBinder localBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
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
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("IR Service Title")
                .setTicker("IR Service Ticker : " + address.getHostAddress())
                .setContentText("IR Service Text").build();

        startForeground(NOTIFICATION_ID, notification);
        WebDaemon daemon = new WebDaemon();
        try {
            daemon.start();
        } catch (IOException e) {
            Log.e("WebServer", "Got IOException while running server", e);
        }
        return START_STICKY;
    }
}
