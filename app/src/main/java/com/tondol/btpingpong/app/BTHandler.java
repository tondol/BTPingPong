package com.tondol.btpingpong.app;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by hosaka on 2014/07/24.
 */
public class BTHandler extends Handler {
    public static final int CLIENT_STARTED = 1;
    public static final int CLIENT_ESTABLISHED = 2;
    public static final int SERVER_STARTED = 3;
    public static final int SERVER_CONNECTED = 4;
    public static final int SERVER_TIMEOUT = 5;
    public static final int MESSAGE_RECEIVED = 6;
    public static final int MESSAGE_ERROR = 7;

    public interface Listener {
        public void onClientStarted(BluetoothDevice device);
        public void onClientEstablished(BluetoothDevice device);
        public void onServerStarted();
        public void onServerConnected();
        public void onServerTimeout();
        public void onMessageReceived(String line);
        public void onMessageError();
    }

    private WeakReference<Listener> mListener;

    public BTHandler(Listener listener) {
        mListener = new WeakReference<Listener>(listener);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Listener listener = mListener.get();
        if (listener == null) {
            return;
        }

        switch (msg.what) {
            case CLIENT_STARTED:
                listener.onClientStarted((BluetoothDevice) msg.obj);
                break;
            case CLIENT_ESTABLISHED:
                listener.onClientEstablished((BluetoothDevice) msg.obj);
                break;
            case SERVER_STARTED:
                listener.onServerStarted();
                break;
            case SERVER_CONNECTED:
                listener.onServerConnected();
                break;
            case SERVER_TIMEOUT:
                listener.onServerTimeout();
                break;
            case MESSAGE_RECEIVED:
                listener.onMessageReceived((String) msg.obj);
                break;
            case MESSAGE_ERROR:
                listener.onMessageError();
                break;
        }
    }
}
