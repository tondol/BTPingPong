package com.tondol.btpingpong.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by hosaka on 2014/07/24.
 */
public class BTClientThread extends BTThread {
    private BluetoothDevice mDevice = null;
    private BluetoothSocket mSocket = null;

    BTClientThread(BTHandler handler, BluetoothDevice device) {
        super(handler);
        mDevice = device;
    }

    @Override
    protected boolean retrieveSocket() {
        Message message = null;

        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(MainActivity.SPP_UUID);
            message = mHandler.obtainMessage(BTHandler.CLIENT_STARTED);
            message.obj = mDevice;
            mHandler.sendMessage(message);
            mSocket.connect();
            message = mHandler.obtainMessage(BTHandler.CLIENT_ESTABLISHED);
            message.obj = mDevice;
            mHandler.sendMessage(message);

            mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
        } catch (IOException e) {
            MainActivity.debug("BTClientThread#retrieveSocket - error - " + e);
            return false;
        }

        return true;
    }

    @Override
    public void ensureDisconnected() {
        super.ensureDisconnected();

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                MainActivity.debug("BTServerThread#ensureDisconnected - socket error - " + e);
            }
        }

        mSocket = null;
    }
}
