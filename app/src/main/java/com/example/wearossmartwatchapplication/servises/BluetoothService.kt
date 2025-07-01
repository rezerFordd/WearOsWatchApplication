package com.example.wearossmartwatchapplication.servises

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import java.io.IOException
import java.util.UUID

class BluetoothService(private val context: Context) {
    private val bluetoothAdapter : BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    companion object{
        val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(deviceFoundCallback : (BluetoothDevice) -> Unit) {
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
    fun connectToDevice(device: BluetoothDevice): BluetoothSocket? {
        val uuid = device.uuids?.firstOrNull()?.uuid ?: MY_UUID
        return try {
            bluetoothAdapter?.cancelDiscovery()
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            socket
        }   catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sendFile(socket: BluetoothSocket, fileUri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val outputStream = socket.outputStream

            if (inputStream != null) {
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0 , bytesRead)
                }
                outputStream.flush()
                inputStream.close()
            }
            outputStream.close()
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}