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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;


public class MainActivity extends Activity implements BTHandler.Listener {
    public static final String TAG = "BTPingPong";
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_BT = 2;
    private static final int REQUEST_DISCOVERABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;
    private SoundManager mSoundManager = null;
    private GLSurfaceView mGLSurfaceView = null;
    private MyRenderer mRenderer = null;

    public static void debug(String text) {
        android.util.Log.d(TAG, text);
    }

    public SoundManager getSoundManager() {
        return mSoundManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mSoundManager = new SoundManager(this);

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

        if (!mBluetoothAdapter.isEnabled()) {
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
            // !開始座標と向きをランダムにする!
            double x = Math.random() * 2.0 - 1.0;
            double y = mRenderer.getDisplayRatio();
            double theta = Math.random() * 2.0 / 3.0 * Math.PI + 7.0 / 6.0 * Math.PI;
            double velocityX = 0.05 * Math.cos(theta);
            double velocityY = 0.05 * Math.sin(theta);
            mRenderer.setSquarePosition(x, y, velocityX, velocityY);
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
    private Handler mHandler = new Handler();

    public void setNetworkState(NetworkState state) {
        mNetworkState = state;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.textview);
                textView.setText("Game: " + mGameState.name() + " / Network: " + mNetworkState.name());
            }
        });
    }

    public GameState getGameState() {
        return mGameState;
    }

    // 通知が必要かどうかを第2引数で指示する
    public void setGameState(GameState state, final boolean sending) {
        mGameState = state;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.textview);
                textView.setText("Game: " + mGameState.name() + " / Network: " + mNetworkState.name());
            }
        });

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
            // !座標系を180度回転させる!
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
