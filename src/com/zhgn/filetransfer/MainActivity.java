
package com.zhgn.filetransfer;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhgn.filetransfer.FileTransferManager.ConnectionListener;
import com.zhgn.filetransfer.FileTransferManager.ReceiveFileListener;
import com.zhgn.filetransfer.FileTransferManager.SendFileListener;
import com.zhgn.filetransfer.FileTransferManager.Type;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends Activity implements PeerListListener, ConnectionInfoListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private Button btnSend = null;
    private Button btnReceive = null;
    private TextView textview1 = null;
    private TextView textview2 = null;
    private TextView textview3 = null;
    private TextView textview4 = null;
    private TextView textview5 = null;
    private TextView textview6 = null;
    private TextView textview7 = null;
    private TextView textview8 = null;
    private TextView textview9 = null;
    private TextView textview10 = null;
    private TextView textview11 = null;
    private TextView textview12 = null;
    private ListView listView = null;

    private final int IP = 1;
    private final int MAC = 2;
    private final int GROUP_OWNER = 3;
    private final int GROUP_OWNER_ADDRESS = 4;
    private final int WIFIP2PDEVICE = 5;
    private final int FILETRANSFER = 6;

    private final String mFileToTransfer = "/sdcard/documents/arp";
    private final String mFileToReceive = "/sdcard/Download/recv_arp";

    private WifiP2pManager mWifiP2pManager = null;
    private WifiP2pManager.Channel mChannel = null;
    private BroadcastReceiver mBroadcastReceiver = null;
    private IntentFilter mIntentFilter = null;

    private boolean mWifiP2pConnectionEnabled = false;
    private boolean mGroupOwner = false;
    private int mPort = 8888;
    private InetAddress mInetAddress = null;
    
    private WifiP2pDeviceAdapter mWifiP2pDeviceAdapter = null;
    private List<WifiP2pDevice> mWifiP2pDeviceList = new ArrayList<WifiP2pDevice>();

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "msg: " + msg.obj);

            if (msg.what == IP) {
            } else if (msg.what == MAC) {
            } else if (msg.what == GROUP_OWNER) {
                textview1.setText("Group owner: " + (String) msg.obj);
            } else if (msg.what == GROUP_OWNER_ADDRESS) {
                InetAddress address = (InetAddress) msg.obj;
                textview2.setText("address: " + address.getHostAddress());
            } else if (msg.what == FILETRANSFER) {
                Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
            } else if (msg.what == WIFIP2PDEVICE) {
                WifiP2pDevice device = (WifiP2pDevice) msg.obj;

                textview3.setText("deviceAddress: " + device.deviceAddress);
                textview4.setText("deviceName: " + device.deviceName);
                textview5.setText("primaryDeviceType: " + device.primaryDeviceType);
                textview6.setText("secondaryDeviceType: " + device.secondaryDeviceType);
                textview7.setText("secondaryDeviceType: " + device.status);
                textview8.setText("isGroupOwner: " + device.isGroupOwner());
                textview9.setText("isServiceDiscoveryCapable: "
                        + device.isServiceDiscoveryCapable());
                textview10.setText("wpsDisplaySupported: " + device.wpsDisplaySupported());
                textview11.setText("wpsKeypadSupported: " + device.wpsKeypadSupported());
                textview12.setText("wpsPbcSupported: " + device.wpsPbcSupported());

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

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        btnSend = (Button) findViewById(R.id.send);
        btnSend.setOnClickListener(new BtnSendListener());

        btnReceive = (Button) findViewById(R.id.receive);
        btnReceive.setOnClickListener(new BtnReceiveListener());

        textview1 = (TextView) findViewById(R.id.textview1);
        textview2 = (TextView) findViewById(R.id.textview2);
        textview3 = (TextView) findViewById(R.id.textview3);
        textview4 = (TextView) findViewById(R.id.textview4);
        textview5 = (TextView) findViewById(R.id.textview5);
        textview6 = (TextView) findViewById(R.id.textview6);
        textview7 = (TextView) findViewById(R.id.textview7);
        textview8 = (TextView) findViewById(R.id.textview8);
        textview9 = (TextView) findViewById(R.id.textview9);
        textview10 = (TextView) findViewById(R.id.textview10);
        textview11 = (TextView) findViewById(R.id.textview11);
        textview12 = (TextView) findViewById(R.id.textview12);

        listView = (ListView) findViewById(R.id.listview1);
        mWifiP2pDeviceAdapter = new WifiP2pDeviceAdapter(this, mWifiP2pDeviceList);
        listView.setAdapter(mWifiP2pDeviceAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                WifiP2pDevice device = (WifiP2pDevice) adapter.getItemAtPosition(pos);

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
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new WifiDirectBroadcastReceiver(mWifiP2pManager,
                mChannel, this);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "disconnect success.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "disconnect failed.");
            }
        });
        mWifiP2pDeviceAdapter.clear();
        mWifiP2pDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discover:
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class BtnSendListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mWifiP2pConnectionEnabled) {
                Log.d(TAG, "send file.");
                Toast.makeText(MainActivity.this, "send file.", Toast.LENGTH_SHORT).show();
                FileTransferManager ftm = null;
                if (mGroupOwner) {
                    ftm = new FileTransferManager(Type.SERVER, mPort, new ConnectionListener() {
                        @Override
                        public void onConnectionSuccess() {
                        }

                        @Override
                        public void onConnectionFailure() {
                        }
                    });
                } else {
                    ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(),
                            mPort, new ConnectionListener() {
                                @Override
                                public void onConnectionSuccess() {
                                }

                                @Override
                                public void onConnectionFailure() {
                                }
                            });
                }
                ftm.sendFile(mFileToTransfer, new SendFileListener() {
                    @Override
                    public void onFileSendSuccess() {
                        Message msg = Message.obtain();
                        msg.what = FILETRANSFER;
                        msg.obj = "send file successfully!";
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFileSendFailure() {
                        Message msg = Message.obtain();
                        msg.what = FILETRANSFER;
                        msg.obj = "send file failed!";
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
    }

    private class BtnReceiveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mWifiP2pConnectionEnabled) {
                Log.d(TAG, "receive file.");
                Toast.makeText(MainActivity.this, "receive file.", Toast.LENGTH_SHORT).show();
                FileTransferManager ftm = null;
                if (mGroupOwner) {
                    ftm = new FileTransferManager(Type.SERVER, mPort, new ConnectionListener() {
                        @Override
                        public void onConnectionSuccess() {
                        }

                        @Override
                        public void onConnectionFailure() {
                        }
                    });
                } else {
                    ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(),
                            mPort, new ConnectionListener() {
                                @Override
                                public void onConnectionSuccess() {
                                }

                                @Override
                                public void onConnectionFailure() {
                                }
                            });
                }
                ftm.receiveFile(mFileToReceive, new ReceiveFileListener() {
                    @Override
                    public void onFileReceiveSuccess() {
                        Message msg = Message.obtain();
                        msg.what = FILETRANSFER;
                        msg.obj = "receive file successfully!";
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFileReceiveFailure() {
                        Message msg = Message.obtain();
                        msg.what = FILETRANSFER;
                        msg.obj = "receive file failed!";
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
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

        mInetAddress = info.groupOwnerAddress;
        mGroupOwner = info.isGroupOwner;

        Message msg = Message.obtain();
        msg.what = GROUP_OWNER_ADDRESS;
        msg.obj = info.groupOwnerAddress;
        mHandler.sendMessage(msg);

        msg = Message.obtain();
        msg.what = GROUP_OWNER;
        msg.obj = String.valueOf(info.isGroupOwner);
        mHandler.sendMessage(msg);

        mWifiP2pConnectionEnabled = true;
    }

}
