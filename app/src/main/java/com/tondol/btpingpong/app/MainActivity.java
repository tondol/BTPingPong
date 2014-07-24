package com.tondol.btpingpong.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;


public class MainActivity extends Activity implements BTHandler.Listener {
    public static final String TAG = "BTPingPong";
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_BT = 2;
    private static final int REQUEST_DISCOVERABLE_BT = 3;

    // デバッグ用メソッドのために静的フィールドに保持しておく
    private static MainActivity sActivity = null;

    private BluetoothAdapter mBA = null;
    private GLSurfaceView mGLSurfaceView = null;
    private MyRenderer mRenderer = null;
    private Handler mHandler = new Handler();

    public static void debug(String text) {
        android.util.Log.d(TAG, text);
        if (sActivity == null) {
            return;
        }
//        TextView textView = (TextView) sActivity.findViewById(R.id.textview_debug);
//        if (textView == null) {
//            return;
//        }
//        if (textView.getText().length() == 0) {
//            textView.setText(text);
//        } else {
//            textView.setText(text + "\n" + textView.getText());
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sActivity = this;
        mBA = BluetoothAdapter.getDefaultAdapter();
        if (mBA == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

//        final EditText editText = (EditText) findViewById(R.id.edittext);
//        Button button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (editText.getText().length() > 0) {
//                    send(editText.getText().toString());
//                }
//                editText.setText("");
//            }
//        });

        mRenderer = new MyRenderer(this);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setRenderer(mRenderer);

        setNetworkState(NetworkState.Default);
        setGameState(GameState.Default, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGLSurfaceView.onResume();

        if (!mBA.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            debug("MainActivity#onResume - Bluetooth is available");
        }
    }

    @Override
    protected void onPause() {
        disconnect();

        mGLSurfaceView.onPause();

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    debug("MainActivity#onActivityResult - RQUEST_ENABLE_BT - OK");
                } else {
                    Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
            case REQUEST_SELECT_BT:
                if (resultCode == RESULT_OK) {
                    debug("MainActivity#onActivityResult - REQUEST_SELECT_BT - OK");
                    BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    connect(device);
                }
                break;
            case REQUEST_DISCOVERABLE_BT:
                if (resultCode != 0) {
                    debug("MainActivity#onActivityResult - REQUEST_DISCOVERABLE_BT - OK");
                    listen();
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point size = new Point();
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);

//        debug("MainActivity#onTouchEvent - " + event);
        mRenderer.setPlayerPosition(event.getX() / (double) size.x * 2.0 - 1.0);

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem listenItem = menu.findItem(R.id.action_listen);
        MenuItem connectItem = menu.findItem(R.id.action_connect);
        MenuItem disconnectItem = menu.findItem(R.id.action_disconnect);
        MenuItem startItem = menu.findItem(R.id.action_start);

        switch (mNetworkState) {
            case Default:
                listenItem.setEnabled(true);
                connectItem.setEnabled(true);
                disconnectItem.setEnabled(false);
                break;
            case Busy:
                listenItem.setEnabled(false);
                connectItem.setEnabled(false);
                disconnectItem.setEnabled(false);
                break;
            case Client:
            case Server:
                listenItem.setEnabled(false);
                connectItem.setEnabled(false);
                disconnectItem.setEnabled(true);
                break;
        }
        switch (mGameState) {
            case Default:
                startItem.setEnabled(mNetworkState == NetworkState.Client ||
                        mNetworkState == NetworkState.Server);
                break;
            case MyTurn:
                startItem.setEnabled(false);
                break;
            case YourTurn:
                startItem.setEnabled(false);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_listen) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(intent, REQUEST_DISCOVERABLE_BT);
            return true;
        } else if (id == R.id.action_connect) {
            Intent intent = new Intent(this, SelectActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_BT);
            return true;
        } else if (id == R.id.action_disconnect) {
            disconnect();
            return true;
        } else if (id == R.id.action_start) {
            mRenderer.setSquarePosition(0.0, 0.0, 0.05, 0.05);
            setGameState(GameState.MyTurn, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public enum GameState {
        Default,
        MyTurn,
        YourTurn,
    }

    public enum NetworkState {
        Default,
        Busy,
        Server,
        Client,
    }

    private NetworkState mNetworkState = NetworkState.Default;
    private GameState mGameState = GameState.Default;

    public void setNetworkState(NetworkState state) {
        mNetworkState = state;
    }

    public GameState getGameState() {
        return mGameState;
    }

    public void setGameState(GameState state, final boolean sending) {
        mGameState = state;
        if (mGameState == GameState.MyTurn) {
            if (sending) {
                send("STATE:MY_TURN");
            }
        } else if (mGameState == GameState.YourTurn) {
            MyRenderer.Square square = mRenderer.getSquare();
            if (sending) {
                send("STATE:YOUR_TURN:" +
                        square.getX() + ":" + square.getY() + ":" +
                        square.getVelocityX() + ":" + square.getVelocityY());
            }
        } else {
            if (sending) {
                send("STATE:DEFAULT");
            }
        }
    }


    private BTServerThread mServerThread = null;
    private BTClientThread mClientThread = null;

    private void listen() {
        debug("MainActivity#listen");
        if (mClientThread == null && mServerThread == null) {
            mServerThread = new BTServerThread(new BTHandler(this));
            mServerThread.start();
        }
    }

    private void connect(BluetoothDevice device) {
        debug("MainActivity#connect - " + device);
        if (mClientThread == null && mServerThread == null) {
            mClientThread = new BTClientThread(new BTHandler(this), device);
            mClientThread.start();
        }
    }

    private void disconnect() {
        debug("MainActivity#disconnect");
        send("DISCONNECT");
        setNetworkState(NetworkState.Default);
        setGameState(GameState.Default, true);

        if (mServerThread != null) {
            mServerThread.ensureDisconnected();
            mServerThread = null;
        }
        if (mClientThread != null) {
            mClientThread.ensureDisconnected();
            mClientThread = null;
        }
    }

    private void send(String line) {
        debug("MainActivity#send - " + line);
        if (mServerThread != null && mServerThread.isConnected()) {
            mServerThread.send(line);
        }
        if (mClientThread != null && mClientThread.isConnected()) {
            mClientThread.send(line);
        }
    }


    @Override
    public void onClientStarted(BluetoothDevice device) {
        debug("MainActivity#onClientStarted - " + device);
        setNetworkState(NetworkState.Busy);
    }

    @Override
    public void onClientEstablished(BluetoothDevice device) {
        debug("MainActivity#onClientEstablished - " + device);
        setNetworkState(NetworkState.Client);
    }

    @Override
    public void onServerStarted() {
        debug("MainActivity#onServerStarted");
        setNetworkState(NetworkState.Busy);
    }

    @Override
    public void onServerConnected() {
        debug("MainActivity#onServerConnected");
        setNetworkState(NetworkState.Server);
    }

    @Override
    public void onServerTimeout() {
        debug("MainActivity#onServerTimeout");
        setNetworkState(NetworkState.Default);
    }

    @Override
    public void onMessageReceived(String line) {
        debug("MainActivity#onMessageReceived - " + line);
        String[] params = line.split(":");
        if (params[0].equals("STATE") && params[1].equals("MY_TURN")) {
            setGameState(GameState.YourTurn, false);
        } else if (params[0].equals("STATE") && params[1].equals("YOUR_TURN")) {
            double x = Double.parseDouble(params[2]);
            double y = Double.parseDouble(params[3]);
            double velocityX = Double.parseDouble(params[4]);
            double velocityY = Double.parseDouble(params[5]);
            // X軸を反転する
            mRenderer.setSquarePosition(-x, mRenderer.getDisplayRatio(), -velocityX, -velocityY);
            setGameState(GameState.MyTurn, false);
        } else if (params[0].equals("STATE") && params[1].equals("DEFAULT")) {
            setGameState(GameState.Default, false);
        } else if (params[0].equals("DISCONNECT")) {
            disconnect();
        }
    }

    @Override
    public void onMessageError() {
        debug("MainActivity#onMessageError");
    }
}
