package com.example.wearossmartwatchapplication.adapters

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver

class BluetoothAdapter(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    companion object {
        const val ACTION_DEVICE_FOUND = BluetoothDevice.ACTION_FOUND
    }

    val bluetoothManager : BluetoothManager by lazy {
        activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    val bluetoothAdapter : BluetoothAdapter?
    @SuppressLint("MissingPermission")
    get() = bluetoothManager.adapter

    private val requestBluetoothConnect = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        if (isGranted) checkBluetoothState()
    }

    private val requestBluetoothScan = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        if (isGranted) startDiscoveryInternal()
    }

    private val enableBluetoothLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(activity, "Bluetooth включён", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkBluetoothState() {
        when {
            bluetoothAdapter == null -> {
                Toast.makeText(activity, "Устройство не поддерживает Bluetooth", Toast.LENGTH_SHORT).show()
            }
            bluetoothAdapter!!.isEnabled == false ->
            {
                if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    enableBluetoothLauncher.launch(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    )
                } else {
                    requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): Set<BluetoothDevice> {
        return if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothAdapter?.bondedDevices ?: emptySet()
        } else {
            requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
            emptySet()
        }
    }

    fun startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            startDiscoveryInternal()
        } else {
            requestBluetoothScan.launch(Manifest.permission.BLUETOOTH_SCAN)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscoveryInternal() {
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun hasPermission(permission: String) :Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}


