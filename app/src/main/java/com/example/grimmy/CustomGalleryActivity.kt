package com.example.grimmy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grimmy.databinding.ActivityCustomGalleryBinding

class CustomGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomGalleryBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_custom_gallery)

        binding.galleryCloseBtnIv.setOnClickListener {
            finish()
        }
    }
}