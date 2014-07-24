package com.tondol.btpingpong.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class SelectActivity extends Activity {
    private BluetoothAdapter mBA = null;
    private ArrayAdapter<BluetoothDevice> mAdapter = null;
    private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        mBA = BluetoothAdapter.getDefaultAdapter();
        mAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.listview_device, mDevices) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_device, parent, false);
                }

                BluetoothDevice device = getItem(position);
                String bonded = device.getBondState() == BluetoothDevice.BOND_BONDED ? " *" : "";
                ((TextView) convertView.findViewById(R.id.device_textview_name)).setText(device.getName() + bonded);
                ((TextView) convertView.findViewById(R.id.device_textview_address)).setText(device.getAddress());
                return convertView;
            }
        };

        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = (BluetoothDevice) listView.getItemAtPosition(i);

                Intent data = new Intent();
                data.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startDiscovery();
    }

    @Override
    protected void onPause() {
        cancelDiscovery();

        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            mDevices.clear();
            mAdapter.notifyDataSetChanged();
            startDiscovery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MainActivity.debug("BroadcastReceiver#onReceive - ACTION_FOUND - " + device);
                mAdapter.add(device);
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                MainActivity.debug("BroadcastReceiver#onReceive - ACTION_DISCOVERY_STARTED");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                MainActivity.debug("BroadcastReceiver#onReceive - ACTION_DISCOVERY_FINISHED");
            }
        }
    };

    private void startDiscovery() {
        MainActivity.debug("SelectActivity#startDiscovery");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (mBA.isDiscovering()) {
            mBA.cancelDiscovery();
        }
        mBA.startDiscovery();
    }

    private void cancelDiscovery() {
        MainActivity.debug("SelectActivity#cancelDiscovery");
        if (mBA.isDiscovering()) {
            mBA.cancelDiscovery();
        }

        unregisterReceiver(mReceiver);
    }
}
