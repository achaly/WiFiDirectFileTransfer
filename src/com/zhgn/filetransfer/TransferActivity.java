
package com.zhgn.filetransfer;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhgn.filetransfer.FileTransferManager.ConnectionListener;
import com.zhgn.filetransfer.FileTransferManager.ReceiveFileListener;
import com.zhgn.filetransfer.FileTransferManager.ReceiveStringListener;
import com.zhgn.filetransfer.FileTransferManager.SendFileListener;
import com.zhgn.filetransfer.FileTransferManager.SendStringListener;
import com.zhgn.filetransfer.FileTransferManager.Type;

import java.net.InetAddress;

public class TransferActivity extends BaseActivity {
    private final String TAG = TransferActivity.class.getSimpleName();

    private Button btnSendString = null;
    private Button btnReceiveString = null;
    private Button btnSendFile = null;
    private Button btnReceiveFile = null;
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
    private TextView textview13 = null;

    private WifiP2pDevice mWifiP2pDevice = null;
    private WifiP2pInfo mWifiP2pInfo = null;

    private boolean mGroupOwner = false;
    private InetAddress mInetAddress = null;
    private final int mPort = 8888;

    private final String mFileToTransfer = "/sdcard/documents/arp";
    private final String mFileToReceive = "/sdcard/Download/recv_arp";
    private final String mStringToSend = "http://www.mi.com/";

    private final int TRANSFER = 0;
    private final int RECEIVE_STRING = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == RECEIVE_STRING) {
                textview13.setText("receive string: " + (String) msg.obj);
                Toast.makeText(TransferActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
            } else if (msg.what == TRANSFER) {
                Toast.makeText(TransferActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        Bundle bundle = getIntent().getExtras();
        mWifiP2pInfo = bundle.getParcelable("WifiP2pInfo");
        mWifiP2pDevice = bundle.getParcelable("WifiP2pDevice");
        
        mGroupOwner = mWifiP2pInfo.isGroupOwner;
        mInetAddress = mWifiP2pInfo.groupOwnerAddress;

        btnSendString = (Button) findViewById(R.id.btnSendString);
        btnReceiveString = (Button) findViewById(R.id.btnReceiveString);
        btnSendFile = (Button) findViewById(R.id.btnSendFile);
        btnReceiveFile = (Button) findViewById(R.id.btnReceiveFile);

        btnSendString.setOnClickListener(new BtnSendStringListener());
        btnReceiveString.setOnClickListener(new BtnReceiveStringListener());
        btnSendFile.setOnClickListener(new BtnSendFileListener());
        btnReceiveFile.setOnClickListener(new BtnReceiveFileListener());

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
        textview13 = (TextView) findViewById(R.id.textview13);

        textview1.setText("isGroupOwner: " + mWifiP2pInfo.isGroupOwner);
        textview2.setText("hostAddress: " + mWifiP2pInfo.groupOwnerAddress.getHostAddress());
        if (mWifiP2pDevice != null) {
            textview3.setText("deviceAddress: " + mWifiP2pDevice.deviceAddress);
            textview4.setText("deviceName: " + mWifiP2pDevice.deviceName);
            textview5.setText("primaryDeviceType: " + mWifiP2pDevice.primaryDeviceType);
            textview6.setText("secondaryDeviceType: " + mWifiP2pDevice.secondaryDeviceType);
            textview7.setText("secondaryDeviceType: " + mWifiP2pDevice.status);
            textview8.setText("isGroupOwner: " + mWifiP2pDevice.isGroupOwner());
            textview9.setText("isServiceDiscoveryCapable: "
                    + mWifiP2pDevice.isServiceDiscoveryCapable());
            textview10.setText("wpsDisplaySupported: " + mWifiP2pDevice.wpsDisplaySupported());
            textview11.setText("wpsKeypadSupported: " + mWifiP2pDevice.wpsKeypadSupported());
            textview12.setText("wpsPbcSupported: " + mWifiP2pDevice.wpsPbcSupported());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "cancel connect successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "cancel connect failed.");
            }
        });
    }

    @Override
    protected void onConnectionChanged(NetworkInfo info) {
        super.onConnectionChanged(info);
        if (info.isConnected()) {
            Log.d(TAG, "connection again.");
        } else {
            Log.e(TAG, "wifi p2p not connected!");
            this.finish();
        }
    }

    private class TransferConnectionListener implements ConnectionListener {
        @Override
        public void onConnectionSuccess() {
        }

        @Override
        public void onConnectionFailure() {
        }
    }

    private class BtnSendStringListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "send string.");
            Toast.makeText(TransferActivity.this, "send String.", Toast.LENGTH_SHORT).show();
            FileTransferManager ftm = null;
            if (mGroupOwner) {
                ftm = new FileTransferManager(Type.SERVER, mPort, new TransferConnectionListener());
            } else {
                ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(), mPort,
                        new TransferConnectionListener());
            }
            ftm.sendString(mStringToSend, new SendStringListener() {
                @Override
                public void onStringSendSuccess(String str) {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "send string successfully!";
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onStringSendFailure(String str) {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "send string failed!";
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    private class BtnReceiveStringListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "receive string.");
            Toast.makeText(TransferActivity.this, "receive String.", Toast.LENGTH_SHORT).show();
            FileTransferManager ftm = null;
            if (mGroupOwner) {
                ftm = new FileTransferManager(Type.SERVER, mPort, new TransferConnectionListener());
            } else {
                ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(), mPort,
                        new TransferConnectionListener());
            }
            ftm.receiveString(new ReceiveStringListener() {
                @Override
                public void onStringReceiveSuccess(String str) {
                    Message msg = Message.obtain();
                    msg.what = RECEIVE_STRING;
                    msg.obj = str;
                    mHandler.sendMessage(msg);

                    msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "receive string successfully!";
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onStringReceiveFailure() {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "receive string failed!";
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    private class BtnSendFileListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "send file.");
            Toast.makeText(TransferActivity.this, "send file.", Toast.LENGTH_SHORT).show();
            FileTransferManager ftm = null;
            if (mGroupOwner) {
                ftm = new FileTransferManager(Type.SERVER, mPort, new TransferConnectionListener());
            } else {
                ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(), mPort,
                        new TransferConnectionListener());
            }
            ftm.sendFile(mFileToTransfer, new SendFileListener() {
                @Override
                public void onFileSendSuccess() {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "send file successfully!";
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onFileSendFailure() {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "send file failed!";
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    private class BtnReceiveFileListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "receive file.");
            Toast.makeText(TransferActivity.this, "receive file.", Toast.LENGTH_SHORT).show();
            FileTransferManager ftm = null;
            if (mGroupOwner) {
                ftm = new FileTransferManager(Type.SERVER, mPort, new TransferConnectionListener());
            } else {
                ftm = new FileTransferManager(Type.CLIENT, mInetAddress.getHostAddress(), mPort,
                        new TransferConnectionListener());
            }
            ftm.receiveFile(mFileToReceive, new ReceiveFileListener() {
                @Override
                public void onFileReceiveSuccess() {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "receive file successfully!";
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onFileReceiveFailure() {
                    Message msg = Message.obtain();
                    msg.what = TRANSFER;
                    msg.obj = "receive file failed!";
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

}
