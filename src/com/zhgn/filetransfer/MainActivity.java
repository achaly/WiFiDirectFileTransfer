
package com.zhgn.filetransfer;

import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends BaseActivity implements PeerListListener, ConnectionInfoListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private Button btnDiscover = null;
    private ListView listView = null;

    private final int WIFIP2PDEVICE = 0;

    private WifiP2pDeviceAdapter mWifiP2pDeviceAdapter = null;
    private List<WifiP2pDevice> mWifiP2pDeviceList = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice mWifiP2pDevice = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == WIFIP2PDEVICE) {
                WifiP2pDevice device = (WifiP2pDevice) msg.obj;
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                mWifiP2pManager.connect(mChannel, config, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "connect callback success.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "connect callback failed.");
                    }
                });

            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDiscover = (Button) findViewById(R.id.btnDiscover);
        btnDiscover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "discover peers.", Toast.LENGTH_SHORT).show();
                mWifiP2pManager.discoverPeers(mChannel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "discover initiated!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "discover failed!");
                    }
                });
            }
        });

        listView = (ListView) findViewById(R.id.listview1);
        mWifiP2pDeviceAdapter = new WifiP2pDeviceAdapter(this, mWifiP2pDeviceList);
        listView.setAdapter(mWifiP2pDeviceAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                WifiP2pDevice device = (WifiP2pDevice) adapter.getItemAtPosition(pos);
                mWifiP2pDevice = device;

                Message msg = Message.obtain();
                msg.what = WIFIP2PDEVICE;
                msg.obj = device;
                mHandler.sendMessage(msg);
            }
        });
    }

    private class WifiP2pDeviceAdapter extends BaseAdapter {
        private List<WifiP2pDevice> mDeviceList;
        private WifiP2pDevice device;
        private LayoutInflater mLayoutInflater;

        public WifiP2pDeviceAdapter(Context context, List<WifiP2pDevice> devicelist) {
            mDeviceList = devicelist;
            mLayoutInflater = LayoutInflater.from(context);
        }

        public void clear() {
            mDeviceList.clear();
        }

        public void addAll(Collection<? extends WifiP2pDevice> collection) {
            mDeviceList.addAll(collection);
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int pos) {
            return mDeviceList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View view, ViewGroup group) {
            device = mDeviceList.get(pos);
            view = mLayoutInflater.inflate(R.layout.list_content, null);

            TextView tv = (TextView) view.findViewById(R.id.displayName);
            tv.setText(device.deviceName);

            return view;
        }

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiP2pDeviceAdapter.clear();
        mWifiP2pDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onConnectionChanged(NetworkInfo info) {
        super.onConnectionChanged(info);
        if (info.isConnected()) {
            mWifiP2pManager.requestConnectionInfo(mChannel, this);
            Log.d(TAG, "request connection info.");
            Toast.makeText(this, "request connection info.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "wifi p2p not connected!");
            Toast.makeText(this, "connect failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPeersChanged() {
        super.onPeersChanged();
        Log.d(TAG, "request peers.");
        mWifiP2pManager.requestPeers(mChannel, this);
        Toast.makeText(this, "request peers.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d(TAG, "onPeersAvailable");
        for (WifiP2pDevice device : peers.getDeviceList()) {
            Log.d(TAG, "deviceName: " + device.deviceName);
            Log.d(TAG, "deviceAddress: " + device.deviceAddress);
            Log.d(TAG, "primaryDeviceType: " + device.primaryDeviceType);
            Log.d(TAG, "secondaryDeviceType: " + device.secondaryDeviceType);
            Log.d(TAG, "status: " + device.status);
            Log.d(TAG, "isGroupOwner: " + device.isGroupOwner());
            Log.d(TAG, "isServiceDiscoveryCapable: " + device.isServiceDiscoveryCapable());
            Log.d(TAG, "wpsDisplaySupported: " + device.wpsDisplaySupported());
            Log.d(TAG, "wpsKeypadSupported: " + device.wpsKeypadSupported());
            Log.d(TAG, "wpsPbcSupported: " + device.wpsPbcSupported());
            Log.d(TAG, " ");
        }
        Log.d(TAG, " ");

        mWifiP2pDeviceAdapter.clear();
        mWifiP2pDeviceAdapter.addAll(peers.getDeviceList());
        mWifiP2pDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "onConnectionInfoAvailable");
        Log.d(TAG, "groupFormed: " + info.groupFormed);
        Log.d(TAG, "groupOwnerAddress: " + info.groupOwnerAddress);
        Log.d(TAG, "isGroupOwner: " + info.isGroupOwner);
        Log.d(TAG, " ");

        Intent intent = new Intent(this, TransferActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("WifiP2pInfo", info);
        bundle.putParcelable("WifiP2pDevice", mWifiP2pDevice);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
