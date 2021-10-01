package com.example.wifi_p2p_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Collection;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = WiFiDirectBroadcastReceiver.class.getSimpleName();

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context context;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context context) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.context = context;
    }

    private boolean isConnected = false;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");
            // Check to see if Wi-Fi is enabled and notify appropriate activity

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d(TAG, "Wifi P2P is enabled");
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "discoverPeers onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "discoverPeers onFailure, reason: " + reason);
                    }
                });

//                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d(TAG, "discoverServices onSuccess");
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Log.e(TAG, "discoverServices onFailure, reason: " + reason);
//                    }
//                });

            } else {
                // Wi-Fi P2P is not enabled
                Log.d(TAG, "Wi-Fi P2P is not enabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            manager.requestPeers(channel, peers -> {
                Collection<WifiP2pDevice> peer = peers.getDeviceList();
                for (WifiP2pDevice device: peer){
                    if (!isConnected)
                        Log.d(TAG, "peers: " + device.toString());
                    if (device.deviceName.equals("Redmi Note 9 Pro") && !isConnected){
//                        if (device.deviceName.equals("MediaPad AP 0001") && !isConnected){
                        isConnected = true;
//                        if (device.deviceAddress.equals("9a:e7:f4:b9:ce:e3") && !isConnected){
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "connect : onSuccess");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "connect : onFailure, reason: " + reason);
                            }
                        });
                    }
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            // Respond to new connection or disconnections
            manager.requestConnectionInfo(channel, info -> {

                new Thread(() -> {
                    if (info.groupOwnerAddress != null) {
                        String string = info.groupOwnerAddress.getHostAddress();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            ((MainActivity) context).textView.setText(string);
                        });
                    }
                }).start();

                Log.d(TAG, "requestConnectionInfo: toString: " + info.toString());

            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                manager.requestNetworkInfo(channel, networkInfo -> {
                    Log.d(TAG, "requestNetworkInfo: toString: " + networkInfo.toString());
                });
            }

            manager.requestGroupInfo(channel, group -> {
                if (group != null) {
                    Log.d(TAG, "requestGroupInfo: " + group.toString());
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                manager.requestDeviceInfo(channel, wifiP2pDevice -> {
                    Log.d(TAG, "requestDeviceInfo: toString: " + wifiP2pDevice.toString());
                });
            }
            // Respond to this device's wifi state changing
        }

    }
}