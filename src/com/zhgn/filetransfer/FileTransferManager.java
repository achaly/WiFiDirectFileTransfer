
package com.zhgn.filetransfer;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class FileTransferManager implements Runnable {

    private final String TAG = FileTransferManager.class.getSimpleName();

    public enum Type {
        CLIENT,
        SERVER,
    }

    private Type mType = null;
    private String mFilepath = null;
    private boolean mSendFlag = false;

    private final int mBufferLength = 102400;
    private byte[] mBuffer = new byte[mBufferLength];
    private int mRead = 0;

    private Socket mSocket = null;
    private SocketAddress mSocketAddress = null;
    private ServerSocket mServerSocket = null;

    private ConnectionListener mConnectionListener = null;
    private SendFileListener mSendFileListener = null;
    private ReceiveFileListener mReceiveFileListener = null;

    public FileTransferManager(Type type, int port, ConnectionListener listener) {
        if (type != Type.SERVER) {
            throw new IllegalArgumentException(
                    "FileTransferManager type error. type should be Type.SERVER .");
        }
        mType = type;
        mSocketAddress = new InetSocketAddress(port);
        mConnectionListener = listener;
    }

    public FileTransferManager(Type type, String ip, int port, ConnectionListener listener) {
        if (type != Type.CLIENT) {
            throw new IllegalArgumentException(
                    "FileTransferManager type error. type should be Type.CLIENT .");
        }
        mType = type;
        mSocketAddress = new InetSocketAddress(ip, port);
        mConnectionListener = listener;
    }

    // file path to send or receive
    public void setFilepath(String filepath) {
        mFilepath = filepath;
    }

    private void sendFile() {
        mSendFlag = true;
        new Thread(this).start();
    }

    public void sendFile(String filepath, SendFileListener listener) {
        mSendFileListener = listener;
        setFilepath(filepath);
        sendFile();
    }

    private void receiveFile() {
        mSendFlag = false;
        new Thread(this).start();
    }

    public void receiveFile(String filepath, ReceiveFileListener listener) {
        mReceiveFileListener = listener;
        setFilepath(filepath);
        receiveFile();
    }

    private void connect() {
        if (mType == Type.SERVER) {
            try {
                mServerSocket = new ServerSocket();
                if (!mServerSocket.isBound()) {
                    mServerSocket.bind(mSocketAddress);
                }
                mSocket = mServerSocket.accept();
                mConnectionListener.onConnectionSuccess();

                Log.d(TAG, "accept socket.");

            } catch (IOException e) {
                mConnectionListener.onConnectionFailure();

                Log.e(TAG, "server socket exception!");
                e.printStackTrace();
            }

        } else {
            try {
                // int timeout = 20 * 1000;
                mSocket = new Socket();
                mSocket.connect(mSocketAddress);
                mConnectionListener.onConnectionSuccess();

                Log.d(TAG, "socket connect successful.");

            } catch (IllegalArgumentException e) {
                mConnectionListener.onConnectionFailure();

                Log.e(TAG, "address or timeout exception.");
                e.printStackTrace();

            } catch (IOException e) {
                mConnectionListener.onConnectionFailure();

                Log.e(TAG, "client socket exception!");
                e.printStackTrace();
            }
        }
    }

    private void disconnect() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        connect();
        if (mSendFlag) {
            BufferedOutputStream bos = null;
            FileInputStream fis = null;
            try {
                bos = new BufferedOutputStream(mSocket.getOutputStream());
                fis = new FileInputStream(mFilepath);

                while ((mRead = fis.read(mBuffer, 0, mBufferLength)) != -1) {
                    bos.write(mBuffer, 0, mRead);
                }

                mSendFileListener.onFileSendSuccess();

            } catch (IOException e) {
                mSendFileListener.onFileSendFailure();

                Log.e(TAG, "send file exception.");
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            try {
                bis = new BufferedInputStream(mSocket.getInputStream());
                fos = new FileOutputStream(mFilepath);

                while ((mRead = bis.read(mBuffer, 0, mBufferLength)) != -1) {
                    fos.write(mBuffer, 0, mRead);
                }

                mReceiveFileListener.onFileReceiveSuccess();

            } catch (IOException e) {
                mReceiveFileListener.onFileReceiveFailure();

                Log.e(TAG, "receive file exception.");
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        disconnect();
    }

    public static interface ConnectionListener {
        public void onConnectionSuccess();

        public void onConnectionFailure();
    }

    public static interface SendFileListener {
        public void onFileSendSuccess();

        public void onFileSendFailure();
    }

    public static interface ReceiveFileListener {
        public void onFileReceiveSuccess();

        public void onFileReceiveFailure();
    }

}
