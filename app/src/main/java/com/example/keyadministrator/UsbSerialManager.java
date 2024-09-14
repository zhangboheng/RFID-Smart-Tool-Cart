package com.example.keyadministrator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import java.nio.ByteBuffer;

public class UsbSerialManager {
    private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface;
    private UsbEndpoint endpointIn;
    private UsbEndpoint endpointOut;
    private PendingIntent permissionIntent;

    public UsbSerialManager(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }

    public void connectToUsbDevice(UsbDevice device) {
        if (usbManager.hasPermission(device)) {
            setupDevice(device);
        } else {
            usbManager.requestPermission(device, permissionIntent);
        }
    }

    private void setupDevice(UsbDevice device) {
        this.device = device;
        usbInterface = device.getInterface(0);
        endpointIn = usbInterface.getEndpoint(0);
        endpointOut = usbInterface.getEndpoint(1);
        connection = usbManager.openDevice(device);
        connection.claimInterface(usbInterface, true);
    }

    public void sendCommand(byte[] command) {
        connection.bulkTransfer(endpointOut, command, command.length, 1000);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            setupDevice(device);
                        }
                    }
                }
            }
        }
    };
}
