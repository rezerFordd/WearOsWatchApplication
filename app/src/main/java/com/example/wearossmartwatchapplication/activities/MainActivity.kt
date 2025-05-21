package com.example.wearossmartwatchapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wearossmartwatchapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
