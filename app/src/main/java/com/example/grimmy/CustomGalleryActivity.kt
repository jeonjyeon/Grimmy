package com.example.grimmy

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grimmy.databinding.ActivityCustomGalleryBinding
import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.grimmy.databinding.ItemImageBinding


class CustomGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomGalleryBinding
    private val imageMap = mutableMapOf<String, MutableList<Uri>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissionsAndLoadImages()
    }

    private fun setupUI() {
        binding.galleryCloseBtnIv.setOnClickListener { finish() }
        binding.galleryToggleLl.setOnClickListener {
            showFolderSelector(imageMap) // imageMap은 폴더 이름과 URI 리스트의 맵
        }
    }

    private fun checkPermissionsAndLoadImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
        } else {
            loadImages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImages()
        } else {
            Log.e("GalleryApp", "Permission denied by user.")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImages() {

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val bucketName = it.getString(bucketColumn)
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                if (!imageMap.containsKey(bucketName)) {
                    imageMap[bucketName] = mutableListOf()
                }
                imageMap[bucketName]?.add(contentUri)
            }
        }

        updateGalleryUI(imageMap)
    }

    private fun updateGalleryUI(images: Map<String, List<Uri>>) {
        // 이미지 맵에서 선택된 폴더의 이름을 키로 사용하여 이미지 리스트를 가져옵니다.
        val selectedFolderImages = images.values.first() // 일단 첫 번째 폴더의 이미지들로 초기화합니다.

        // RecyclerView에 어댑터 설정
        binding.galleryAllImgRv.layoutManager = GridLayoutManager(this, 3)
        binding.galleryAllImgRv.adapter = ImageAdapter(selectedFolderImages)
    }

    private fun showFolderSelector(imageMap: Map<String, List<Uri>>) {
        val folders = imageMap.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select a Folder")
            .setItems(folders) { dialog, which ->
                val selectedFolder = folders[which]
                updateGalleryUI(mapOf(selectedFolder to imageMap[selectedFolder]!!))
            }
            .show()
    }

    inner class ImageAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(imageUris[position])
                .into(holder.imageView)
        }

        override fun getItemCount() = imageUris.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.image_view)
        }
    }
}
