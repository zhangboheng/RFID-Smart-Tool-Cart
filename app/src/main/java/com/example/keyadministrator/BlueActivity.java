package com.example.keyadministrator;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.peripheral.ble.BleDevice;
import com.peripheral.ble.BleServiceCallback;
import com.peripheral.ble.BluetoothCentralManager;
import com.peripheral.ble.BluetoothCentralManagerCallback;
import com.peripheral.ble.BluetoothPeripheral;
import com.peripheral.ble.HciStatus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BlueActivity extends AppCompatActivity {


    GClient client = new GClient();
    private BluetoothCentralManager central;

    BluetoothCentralManagerCallback centralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
            // TODO: 2022/5/19 此处只演示扫描到指定名称或地址(peripheral.getAddress())的ble设备立即连接,（可以将搜索的设备用集合存储起来，再自主连接）
            //已经建立的连接需要close掉才能再次搜索到
            if (!peripheral.getName().isEmpty() && peripheral.getName().equals("Feasycom")) {
                central.stopScan();

                BleDevice bleDevice = new BleDevice(central, peripheral);
                //指定serviceUUID,不需要再setServiceCallback()筛选确定可用BluetoothGattCharacteristic
                //示例"0000fff0-0000-1000-8000-00805f9b34fb"
//                bleDevice.setServiceUuid("0000fff0-0000-1000-8000-00805f9b34fb");

                //未指定serviceUUID,需要自己筛选可用BluetoothGattCharacteristic
                bleDevice.setServiceCallback(new BleServiceCallback() {
                    @Override
                    public void onServicesDiscovered(BluetoothPeripheral peripheral) {
                        List<BluetoothGattService> services = peripheral.getServices();
                        for (BluetoothGattService service : services) {
                            //示例"0000fff0-0000-1000-8000-00805f9b34fb"
                            if (service.getUuid().toString().equals("0000fff0-0000-1000-8000-00805f9b34fb")) {
                                bleDevice.findCharacteristic(service);
                            }
                        }
                        bleDevice.setNotify(true);
                    }
                });
                client.openBleDevice(bleDevice);
            }
        }

        @Override
        public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
            Log.e(peripheral.getName(), "连接成功");
        }

        @Override
        public void onDisconnectedPeripheral(BluetoothPeripheral peripheral, HciStatus status) {
            Log.e(peripheral.getName(), "断开连接");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_reader);
        Button scan = (Button) findViewById(R.id.scan);
        Button stop = (Button) findViewById(R.id.stop);
        Button readCard = (Button) findViewById(R.id.readCard);
        Button stopCard = (Button) findViewById(R.id.stopCard);
        subscriberHandler();

        central = new BluetoothCentralManager(this, centralManagerCallback, new Handler(Looper.getMainLooper()));

        //按需申请权限
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        //permissionItems.add(new PermissionItem(Manifest.permission.ACCESS_COARSE_LOCATION, "位置", R.drawable.permission_ic_location));
        HiPermission.create(this)
                .title("开启蓝牙所需权限")
                .permissions(permissionItems)
                .msg("蓝牙搜索需要开启位置权限")
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onDeny(String permission, int position) {
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                    }
                });

        scan.setOnClickListener(view -> {
            central.scanForPeripherals();
        });

        stop.setOnClickListener(view -> {
            central.stopScan();
        });


        readCard.setOnClickListener(v -> {
            MsgBaseInventoryEpc msg = new MsgBaseInventoryEpc();
            msg.setAntennaEnable(EnumG.AntennaNo_1);
            msg.setInventoryMode(EnumG.InventoryMode_Inventory);
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                Log.e("MsgBaseInventoryEpc", "Inventory success");
            }
        });

        stopCard.setOnClickListener(v -> {
            MsgBaseStop msg = new MsgBaseStop();
            client.sendSynMsg(msg);
            Log.e("MsgBaseStop", msg.getRtMsg());
        });


    }

    private void subscriberHandler() {
        client.onTagEpcLog = new HandlerTagEpcLog() {
            @Override
            public void log(String s, LogBaseEpcInfo logBaseEpcInfo) {
                if (logBaseEpcInfo.getResult() == 0) {
                    Log.e("epc", logBaseEpcInfo.getEpc());
                }
            }
        };
        client.onTagEpcOver = new HandlerTagEpcOver() {
            @Override
            public void log(String s, LogBaseEpcOver logBaseEpcOver) {
                Log.e("HandlerTagEpcOver", logBaseEpcOver.getRtMsg());
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
        central.close();
    }
}