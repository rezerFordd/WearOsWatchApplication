package com.example.wearossmartwatchapplication.servises

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.util.UUID

class BluetoothService(private val context: Context) {

    private var connectedGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    companion object {
        val GENERIC_FILE_TRANSFER_SERVICE_UUID: UUID = UUID.fromString("0000FEFF-0000-1000-8000-00805F9B34FB")
        val GENERIC_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(deviceFoundCallback: (BluetoothDevice) -> Unit) {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { deviceFoundCallback(it) }
                }
            }
        }
        context.registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        connectedDevice = device
        connectedGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("BluetoothService", "Connected to device")
                    gatt?.discoverServices()
                } else {
                    Log.d("BluetoothService", "Disconnected from device")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothService", "Services discovered")
                    val service = gatt?.getService(GENERIC_FILE_TRANSFER_SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(GENERIC_CHARACTERISTIC_UUID)
                    characteristic?.let {
                        gatt.readCharacteristic(it)
                    }
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val value = characteristic?.value
                    Log.d("BluetoothService", "Characteristic read: ${value?.joinToString()}")
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothService", "Characteristic write successful")
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun sendFile(fileUri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val outputStreamCharacteristic = connectedGatt?.getService(GENERIC_FILE_TRANSFER_SERVICE_UUID)?.getCharacteristic(GENERIC_CHARACTERISTIC_UUID)

            if (inputStream != null && outputStreamCharacteristic != null) {
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStreamCharacteristic.value = buffer.copyOf(bytesRead)
                    connectedGatt?.writeCharacteristic(outputStreamCharacteristic)
                }

                inputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun closeConnection() {
        connectedGatt?.close()
        connectedGatt = null
        Log.d("BluetoothService", "Connection closed")
    }

    fun getConnectedGatt(): BluetoothGatt? {
        return connectedGatt
    }

}