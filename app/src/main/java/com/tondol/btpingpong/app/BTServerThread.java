package com.tondol.btpingpong.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
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
public class BTServerThread extends BTThread {
    private BluetoothAdapter mBA = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mSocket = null;

    public BTServerThread(BTHandler handler) {
        super(handler);
    }

    @Override
    protected boolean retrieveSocket() {
        Message message = null;

        try {
            message = mHandler.obtainMessage(BTHandler.SERVER_STARTED);
            mHandler.sendMessage(message);
            BluetoothServerSocket serverSock = mBA.listenUsingRfcommWithServiceRecord(mBA.getName(), MainActivity.SPP_UUID);
            mSocket = serverSock.accept(30000);
            message = mHandler.obtainMessage(BTHandler.SERVER_CONNECTED);
            mHandler.sendMessage(message);
            serverSock.close();

            mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
        } catch (IOException e) {
            android.util.Log.i(MainActivity.TAG, "BTServerThread#retrieveSocket - error - " + e);
            message = mHandler.obtainMessage(BTHandler.SERVER_TIMEOUT);
            mHandler.sendMessage(message);
            return false;
        }

        return true;
    }
}
