package com.example.wearossmartwatchapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wearossmartwatchapplication.databinding.ActivityMainBinding
import com.example.wearossmartwatchapplication.servises.BluetoothService
import com.example.wearossmartwatchapplication.adapters.DeviceAdapter
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothService: BluetoothService
    private var connectedGatt = bluetoothService.getConnectedGatt()
    private var devices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: DeviceAdapter

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothService = BluetoothService(this)

        if (!hasPermissions()) {
            requestPermissions()
        }

        @SuppressLint("MissingPermission")
        deviceAdapter = DeviceAdapter(devices) { device ->
            bluetoothService.connectToDevice(device)
            binding.buttonSelectFile.isEnabled = true
            Toast.makeText(this, "Подключено к ${device.name} !", Toast.LENGTH_SHORT).show()
        }

        binding.rcViewDevices.layoutManager = LinearLayoutManager(this)
        binding.rcViewDevices.adapter = deviceAdapter

        binding.buttonSearch.setOnClickListener {
            devices.clear()
            deviceAdapter.notifyDataSetChanged()
            bluetoothService.startDiscovery { device ->
                runOnUiThread {
                    if (!devices.any { it.address == device.address }) {
                        devices.add(device)
                        deviceAdapter.notifyItemInserted(devices.lastIndex)
                    }
                }
            }
        }

        binding.buttonSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/vnd.android.package-archive"
            }
            selectFileResult.launch(intent)
        }
    }

    private val selectFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileUri: Uri? = result.data?.data
            fileUri?.let {
                if (connectedGatt != null) {
                    bluetoothService.sendFile(it)
                    Toast.makeText(this, "Файл отправлен !", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Файл не выбран !", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.cancelDiscovery()
        bluetoothService.closeConnection()
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Разрешения предоставлены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не все разрешения предоставлены", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
