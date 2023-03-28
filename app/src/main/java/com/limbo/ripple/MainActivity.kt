package com.limbo.ripple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.limbo.ripple.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rippleView.start()
    }
}