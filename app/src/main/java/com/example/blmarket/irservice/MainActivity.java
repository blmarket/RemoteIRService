package com.example.blmarket.irservice;

import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.blmarket.irservice.service.WebServer;


public class MainActivity extends ActionBarActivity {
    private static final String TAG_NAME = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(getApplicationContext(), WebServer.class));

        final ConsumerIrManager service = (ConsumerIrManager) getApplicationContext().getSystemService(Context.CONSUMER_IR_SERVICE);
        Log.d(TAG_NAME, "hasIrEmitter : " + service.hasIrEmitter());
        for (ConsumerIrManager.CarrierFrequencyRange fRange : service.getCarrierFrequencies()) {
            Log.d(TAG_NAME, "FREQ RANGE : " + fRange.getMinFrequency() + " " + fRange.getMaxFrequency());
        }
        String[] codes = "345 170 24 20 22 20 22 20 22 20 22 20 22 20 22 20 22 63 23 62 23 64 22 63 23 63 23 63 22 63 23 62 23 20 22 63 23 20 22 20 22 20 22 20 22 20 22 20 22 20 22 20 22 63 23 63 22 63 23 62 24 62 23 62 23 63 23 1536 345 84 23 3678 345 170 24 20 22 20 22 20 22 20 22 20 22 20 22 20 22 62 23 64 22 63 23 63 22 63 23 62 23 64 22 63 23 20 23 63 22 63 23 63 23 63 22 63 23 63 23 63 23 62 23 20 22 20 22 20 22 20 22 20 22 20 22 20 22 20 22 3797".split(" ");
        final int[] ircode = new int[codes.length];
        for (int i = 0; i < codes.length; i++) {
            ircode[i] = Integer.parseInt(codes[i]);
        }
        final Boolean[] sent = {false};
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.button_test_irsend);
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG_NAME, "button touched");
                if (!sent[0]) {
                    service.transmit(38000, ircode);
                }
                // if(!sent[0]) ir.sendCommand(cmd);
                sent[0] = true;
                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
