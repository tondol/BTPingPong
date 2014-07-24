package com.tondol.btpingpong.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;


public class MainActivity extends Activity implements BTHandler.Listener {
    public static final String TAG = "BTPingPong";
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_BT = 2;

    private BluetoothAdapter mBA = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mBA == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.edittext);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().length() > 0) {
                    send(editText.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBA.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            android.util.Log.d(TAG, "MainActivity#onResume - Bluetooth is available");
        }
    }

    @Override
    protected void onPause() {
        disconnect();

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    android.util.Log.d(TAG, "MainActivity#onActivityResult - Bluetooth is available");
                } else {
                    Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
            case REQUEST_SELECT_BT:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    connect(device);
                } else {
                    Toast.makeText(this, "Device is not available", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_listen) {
            listen();
            return true;
        } else if (id == R.id.action_connect) {
            Intent intent = new Intent(this, SelectActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_BT);
            return true;
        } else if (id == R.id.action_disconnect) {
            disconnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private BTServerThread mServerThread = null;
    private BTClientThread mClientThread = null;

    private void listen() {
        android.util.Log.d(TAG, "MainActivity#listen");
        mServerThread = new BTServerThread(new BTHandler(this));
        mServerThread.start();
    }

    private void connect(BluetoothDevice device) {
        android.util.Log.d(TAG, "MainActivity#connect - " + device);
        mClientThread = new BTClientThread(new BTHandler(this), device);
        mClientThread.start();
    }

    private void disconnect() {
        android.util.Log.d(TAG, "MainActivity#disconnect");
        if (mServerThread != null && mServerThread.isConnected()) {
            mServerThread.close();
        }
        if (mClientThread != null && mClientThread.isConnected()) {
            mClientThread.close();
        }
    }

    private void send(String line) {
        android.util.Log.d(TAG, "MainActivity#send - " + line);
        if (mServerThread != null && mServerThread.isConnected()) {
            mServerThread.send(line);
        }
        if (mClientThread != null && mClientThread.isConnected()) {
            mClientThread.send(line);
        }
    }


    @Override
    public void onClientStarted(BluetoothDevice device) {
        android.util.Log.d(TAG, "MainActivity#onClientStarted - " + device);
    }

    @Override
    public void onClientEstablished(BluetoothDevice device) {
        android.util.Log.d(TAG, "MainActivity#onClientEstablished - " + device);
    }

    @Override
    public void onServerStarted() {
        android.util.Log.d(TAG, "MainActivity#onServerStarted");
    }

    @Override
    public void onServerConnected() {
        android.util.Log.d(TAG, "MainActivity#onServerConnected");
    }

    @Override
    public void onServerTimeout() {
        android.util.Log.d(TAG, "MainActivity#onServerTimeout");
    }

    @Override
    public void onMessageReceived(String line) {
        android.util.Log.d(TAG, "MainActivity#onMessageReceived - " + line);
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(line);
    }

    @Override
    public void onMessageError() {
        android.util.Log.d(TAG, "MainActivity#onMessageError");
    }
}
