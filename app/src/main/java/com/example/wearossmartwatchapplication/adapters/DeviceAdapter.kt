package com.example.wearossmartwatchapplication.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wearossmartwatchapplication.R

class DeviceAdapter (
    private val devices: List<BluetoothDevice>,
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.deviceName)
        val macAddress: TextView = itemView.findViewById(R.id.deviceAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.name.text = device.name ?: "Безымянное устройство"
        holder.macAddress.text = device.address

        holder.itemView.setOnClickListener{
            onDeviceClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size
}
