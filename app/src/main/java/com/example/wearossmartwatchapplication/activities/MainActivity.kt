package com.example.wearossmartwatchapplication.activities


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wearossmartwatchapplication.databinding.ActivityMainBinding
import com.example.wearossmartwatchapplication.servises.BluetoothService
import com.example.wearossmartwatchapplication.adapters.DeviceAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.app.Activity
import android.net.Uri

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothService: BluetoothService
    private var connectedSocket = bluetoothService.getConnectedSocket()
    private var devices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bluetoothService = BluetoothService(this)

        @SuppressLint("MissingPermission")
        deviceAdapter = DeviceAdapter(devices) { device ->
            val socket = bluetoothService.connectToDevice(device)
            if (socket != null) {
                connectedSocket = socket
                binding.buttonSelectFile.isEnabled = true
                Toast.makeText(this, "Подключено к ${device.name} !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось подключиться к устройству !", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rcViewDevices.layoutManager = LinearLayoutManager(this)
        binding.rcViewDevices.adapter = deviceAdapter

        binding.buttonSearch.setOnClickListener{
            devices.clear()
            deviceAdapter.notifyDataSetChanged()
            bluetoothService.startDiscovery { device ->
                runOnUiThread{
                    if (!devices.any{it.address == device.address}) {
                        devices.add(device)
                        deviceAdapter.notifyItemInserted(devices.lastIndex)
                    }
                }
            }
        }

        binding.buttonSelectFile.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/vnd.android.package-archive"
            }
            selectFileResult.launch(intent)
        }
    }

    private val selectFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileUri : Uri? = result.data?.data
            fileUri?.let {
                if (connectedSocket != null) {
                    bluetoothService.sendFile(connectedSocket!!, it)
                    Toast.makeText(this, "Файл отправлен !", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Файл не выбран !", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.closeConnection(connectedSocket)
    }
}
