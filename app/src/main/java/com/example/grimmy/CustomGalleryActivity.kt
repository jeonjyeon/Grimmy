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
    private var imageAdapter: ImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissionsAndLoadImages()
    }

    private fun setupUI() {
        // 닫기 버튼 클릭 시 종료
        binding.galleryCloseBtnIv.setOnClickListener { finish() }
        // 폴더 선택 레이아웃 클릭 시 다이얼로그 표시
        binding.galleryToggleLl.setOnClickListener {
            showFolderSelector(imageMap) // imageMap은 폴더 이름과 해당 폴더의 이미지 URI 리스트를 담고 있음
        }
        // RecyclerView 설정 (그리드 레이아웃)
        binding.galleryAllImgRv.layoutManager = GridLayoutManager(this, 3)
        // ImageAdapter 생성 시 선택 상태 변경 콜백을 전달 (선택된 개수에 따라 완료 버튼 이미지를 업데이트)
        imageAdapter = ImageAdapter(listOf(), this) { selectedCount ->
            if (selectedCount > 0) {
                binding.galleryNotFinishIv.setImageResource(R.drawable.btn_gallery_finish) // 활성화된 완료 버튼 이미지
            } else {
                binding.galleryNotFinishIv.setImageResource(R.drawable.btn_gallery_finish_off) // 비활성화된 완료 버튼 이미지
            }
        }
        binding.galleryAllImgRv.adapter = imageAdapter

        // 완료 버튼 클릭 시 선택된 이미지 목록을 결과로 전달
        binding.galleryNotFinishIv.setOnClickListener {
            val selectedImages = imageAdapter?.getSelectedImages() ?: emptyList()
            if (selectedImages.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putParcelableArrayListExtra("selectedImages", ArrayList(selectedImages))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
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
                // 권한이 허용된 경우 이미지 로딩
                loadImages()
            } else {
                // 권한 거부 시 안내 메시지
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder(this)
                        .setTitle("권한 필요")
                        .setMessage("이 앱은 갤러리에 접근하기 위해 저장소 권한이 필요합니다. 앱 설정에서 권한을 허용해 주세요.")
                        .setPositiveButton("설정") { dialog, which ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("취소", null)
                        .show()
                } else {
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
            Toast.makeText(this, "어떤 폴더에도 이미지가 없습니다.", Toast.LENGTH_LONG).show()
            return
        }
        // 기본적으로 첫 번째 폴더 선택
        val selectedFolder = images.keys.first()
        binding.galleryFileNameTv.text = selectedFolder
        val selectedFolderImages = images[selectedFolder]!!
        imageAdapter?.updateData(selectedFolderImages)
    }

    private fun showFolderSelector(imageMap: Map<String, List<Uri>>) {
        val folders = imageMap.filter { it.value.isNotEmpty() }.keys.toTypedArray()
        if (folders.isEmpty()) {
            Toast.makeText(this, "어떤 폴더에도 이미지가 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("폴더 선택")
            .setItems(folders) { dialog, which ->
                val selectedFolder = folders[which]
                // 선택한 폴더명을 TextView에 표시
                binding.galleryFileNameTv.text = selectedFolder
                updateGalleryUI(mapOf(selectedFolder to imageMap[selectedFolder]!!))
            }
            .show()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    inner class ImageAdapter(
        private var imageUris: List<Uri>,
        private val activity: AppCompatActivity,
        private val onSelectionChanged: (Int) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        private val TYPE_CAMERA = 0
        private val TYPE_IMAGE = 1

        // 선택된 이미지들을 순서를 유지하면서 저장 (선택 순서 표시를 위해 List 사용)
        private val selectedImages = mutableListOf<Uri>()

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) TYPE_CAMERA else TYPE_IMAGE
        }

        fun updateData(newImageUris: List<Uri>) {
            imageUris = newImageUris
            selectedImages.clear() // 데이터 갱신 시 선택 초기화
            onSelectionChanged(selectedImages.size)
            notifyDataSetChanged()
        }

        fun getSelectedImages(): List<Uri> = selectedImages.toList()

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
                val actualPosition = position - 1
                val imageUri = imageUris[actualPosition]
                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .into(holder.imageView!!)

                // 선택된 이미지라면 오버레이 숫자와 주황색 윤곽선 표시
                if (selectedImages.contains(imageUri)) {
                    val order = selectedImages.indexOf(imageUri) + 1
                    holder.selectionOrderTv?.apply {
                        visibility = View.VISIBLE
                        text = order.toString()
                    }
                    holder.itemView.alpha = 0.8f
                    // 주황색 윤곽선 추가
                    holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.selected_border)
                } else {
                    holder.selectionOrderTv?.visibility = View.GONE
                    holder.itemView.alpha = 1.0f
                    // 윤곽선 제거 (기본 배경으로 설정)
                    holder.itemView.background = null
                }

                holder.itemView.setOnClickListener {
                    // 아직 선택되지 않은 경우
                    if (!selectedImages.contains(imageUri)) {
                        if (selectedImages.size >= 3) {
                            Toast.makeText(activity, "최대 3개만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        selectedImages.add(imageUri)
                    } else {
                        selectedImages.remove(imageUri)
                    }
                    onSelectionChanged(selectedImages.size)
                    notifyItemChanged(position)
                }
            } else {
                // 카메라 아이콘 클릭 시 처리
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

        override fun getItemCount() = imageUris.size + 1 // 첫 번째 아이템은 카메라 아이콘

        inner class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            // 이미지 아이템일 경우 이미지뷰와 선택 순서 오버레이 TextView 참조
            val imageView: ImageView? = if (viewType == TYPE_IMAGE) itemView.findViewById(R.id.image_view) else null
            val selectionOrderTv: TextView? = if (viewType == TYPE_IMAGE) itemView.findViewById(R.id.selection_order_tv) else null
        }
    }
}