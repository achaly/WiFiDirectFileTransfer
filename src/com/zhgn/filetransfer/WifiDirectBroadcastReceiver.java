
package com.zhgn.filetransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = WifiDirectBroadcastReceiver.class.getSimpleName();

    private WifiP2pManager mWifiP2pManager = null;
    private Channel mChannel = null;
    private MainActivity mMainActivity = null;

    public WifiDirectBroadcastReceiver(WifiP2pManager m, Channel c, MainActivity a) {
        mWifiP2pManager = m;
        mChannel = c;
        mMainActivity = a;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");

            // UI update to indicate wifi p2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "wifi p2p enable.");
                Toast.makeText(mMainActivity, "wifi p2p enable.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "wifi p2p not enable.");
                Toast.makeText(mMainActivity, "wifi p2p not enable.", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            mWifiP2pManager.requestPeers(mChannel, mMainActivity);
            Log.d(TAG, "request peers.");
            Toast.makeText(mMainActivity, "request peers.", Toast.LENGTH_SHORT).show();

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");

            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (info.isConnected()) {
                mWifiP2pManager.requestConnectionInfo(mChannel, mMainActivity);
                Log.d(TAG, "request connection info.");
                Toast.makeText(mMainActivity, "request connection info.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Log.e(TAG, "wifi p2p not connected!");
                Toast.makeText(mMainActivity, "connect failed!", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

        }
    }

}
