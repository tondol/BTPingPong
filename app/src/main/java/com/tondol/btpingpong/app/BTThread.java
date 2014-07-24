package com.tondol.btpingpong.app;

import android.bluetooth.BluetoothAdapter;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by hosaka on 2014/07/24.
 */
abstract public class BTThread extends Thread {
    protected BTHandler mHandler = null;
    protected BufferedReader mReader = null;
    protected BufferedWriter mWriter = null;
    protected boolean mStopped = false;

    public BTThread(BTHandler handler) {
        mHandler = handler;
    }

    abstract protected boolean retrieveSocket();

    protected void receiveMessage() {
        Message message = null;

        try {
            while (true) {
                while (!mStopped && !mReader.ready()) {
                    Thread.sleep(100);
                }
                if (mStopped) {
                    break;
                }

                String line = mReader.readLine();
                message = mHandler.obtainMessage(BTHandler.MESSAGE_RECEIVED);
                message.obj = line;
                mHandler.sendMessage(message);
            }

            android.util.Log.i(MainActivity.TAG, "BTThread#receiveMessage - finished");
        }
        catch (Exception e) {
            android.util.Log.i(MainActivity.TAG, "BTThread#receiveMessage - error - " + e);
            message = mHandler.obtainMessage(BTHandler.MESSAGE_ERROR);
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void run() {
        super.run();

        if (retrieveSocket()) {
            receiveMessage();
        }

        close();
    }


    public boolean isConnected() {
        return !mStopped && mReader != null && mWriter != null;
    }

    public void close() {
        mStopped = true;

        if (mReader != null) {
            try {
                mReader.close();
            } catch (IOException e) {
                android.util.Log.d(MainActivity.TAG, "BTThread#close - close(reader) error - " + e);
            }
        }
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                android.util.Log.d(MainActivity.TAG, "BTThread#close - close(writer) error - " + e);
            }
        }
    }

    public void send(String line) {
        if (isConnected()) {
            try {
                mWriter.write(line, 0, line.length());
                mWriter.newLine();
            } catch (IOException e) {
                android.util.Log.d(MainActivity.TAG, "BTThread#send - writer error - " + e);
            }
        }
    }
}
