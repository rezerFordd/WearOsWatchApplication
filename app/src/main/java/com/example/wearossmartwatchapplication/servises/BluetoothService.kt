package com.example.wearossmartwatchapplication.servises

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.Uri

class BluetoothService(private val context: Context) {
    private val bluetoothAdapter : BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    fun startDiscovery(deviceFoundCallback : (BluetoothDevice) -> Unit) {

    }

    fun connectToDevice(device: BluetoothDevice): BluetoothSocket? {

    }

    fun sendFile(socket: BluetoothSocket, fileUri: Uri) {

    }
}