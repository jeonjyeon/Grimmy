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
        // 초기 RecyclerView 설정
        binding.galleryAllImgRv.layoutManager = GridLayoutManager(this, 3)
        binding.galleryAllImgRv.adapter = ImageAdapter(listOf(), this) // 초기 빈 리스트로 아댑터 설정
    }

    private fun checkPermissionsAndLoadImages() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val allPermissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 1)
        } else {
            loadImages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우, 이미지 로딩
                loadImages()
            } else {
                // 권한이 거부된 경우, 사용자에게 추가 정보 제공 및 설정 유도
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // 권한 요청에 대한 추가 설명이 필요한 경우
                    AlertDialog.Builder(this)
                        .setTitle("권한 필요")
                        .setMessage("이 앱은 갤러리에 접근하기 위해 저장소 권한이 필요합니다. 앱 설정에서 권한을 허용해 주세요.")
                        .setPositiveButton("설정") { dialog, which ->
                            // 설정 화면으로 이동
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("취소", null)
                        .show()
                } else {
                    // 사용자가 권한 요청을 거부하고 다시 묻지 않음을 선택한 경우
                    Toast.makeText(this, "권한이 거부되었습니다. 설정에서 권한을 활성화할 수 있습니다.", Toast.LENGTH_LONG).show()
                }
            }
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
        if (images.isEmpty() || images.values.any { it.isEmpty() }) {
            Toast.makeText(this, "No images found in any folder.", Toast.LENGTH_LONG).show()
            return
        }

        val selectedFolderImages = images.values.first()

        // 데이터가 준비된 후에 아댑터를 설정 또는 업데이트
        if (binding.galleryAllImgRv.adapter == null) {
            binding.galleryAllImgRv.adapter = ImageAdapter(selectedFolderImages, this)
        } else {
            (binding.galleryAllImgRv.adapter as ImageAdapter).updateData(selectedFolderImages)
        }
    }

    private fun showFolderSelector(imageMap: Map<String, List<Uri>>) {
        val folders = imageMap.filter { it.value.isNotEmpty() }.keys.toTypedArray()
        if (folders.isEmpty()) {
            Toast.makeText(this, "No images found in any folder.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Select a Folder")
            .setItems(folders) { dialog, which ->
                val selectedFolder = folders[which]
                updateGalleryUI(mapOf(selectedFolder to imageMap[selectedFolder]!!))
            }
            .show()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    inner class ImageAdapter(private var imageUris: List<Uri>, private val activity: AppCompatActivity) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        private val TYPE_CAMERA = 0
        private val TYPE_IMAGE = 1

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) TYPE_CAMERA else TYPE_IMAGE
        }

        fun updateData(newImageUris: List<Uri>) {
            imageUris = newImageUris
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = if (viewType == TYPE_CAMERA) {
                LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
            } else {
                LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            }
            return ViewHolder(view, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (getItemViewType(position) == TYPE_IMAGE) {
                Glide.with(holder.itemView.context)
                    .load(imageUris[position - 1])
                    .into(holder.imageView!!)
            } else {
                holder.itemView.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    } else {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }

        override fun getItemCount() = imageUris.size + 1 // +1 for camera icon

        inner class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView? = if (viewType == TYPE_IMAGE) itemView.findViewById(R.id.image_view) else null
        }
    }

}