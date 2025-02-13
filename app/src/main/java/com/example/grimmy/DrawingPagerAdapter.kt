package com.example.grimmy

import android.net.Uri
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grimmy.Retrofit.Request.DailyCommentSaveRequest
import com.example.grimmy.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DrawingPagerAdapter(
    private val images: List<Uri>,
    private val dailyId: Int
) : RecyclerView.Adapter<DrawingPagerAdapter.ViewHolder>() {

    // 각 페이지별로 코멘트를 저장할 자료구조 (좌표, 제목, 내용)
    data class Comment(val x: Float, val y: Float, val title: String, val content: String)
    private val comments: MutableList<Comment> = mutableListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: FrameLayout = itemView.findViewById(R.id.comment_container_fl)
        val imageView: ImageView = itemView.findViewById(R.id.drawing_page_iv)
        // 각 페이지별 코멘트 목록 (필요시 외부 저장소에 저장하거나, 서버 연동 고려)
        val comments = mutableListOf<Comment>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawing_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Glide를 이용해 이미지 로드
        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.imageView)

        // 기존 오버레이 제거 (뷰 재활용 대응)
        while (holder.container.childCount > 1) {
            holder.container.removeViewAt(1)
        }

        // 저장된 코멘트들을 화면에 오버레이로 추가
        for (comment in holder.comments) {
            addCommentOverlay(holder.container, comment)
        }

        // GestureDetector 생성 부분: onSingleTapConfirmed의 파라미터를 MotionEvent로 변경
        val gestureDetector = GestureDetector(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {  // 수정된 부분
                Log.d("DrawingPagerAdapter", "onSingleTapConfirmed: ${e.rawX}, ${e.rawY}")
                // 이미지 내부에서의 터치 좌표 계산 (container의 좌표계 기준)
                val containerCoords = IntArray(2)
                holder.container.getLocationOnScreen(containerCoords)
                val touchX = e.rawX - containerCoords[0]
                val touchY = e.rawY - containerCoords[1]

                // 터치한 위치가 container 내부인지 확인 (정확히 이미지 위에서만 반응)
                if (touchX >= 0 && touchY >= 0 &&
                    touchX <= holder.container.width && touchY <= holder.container.height) {
                    // 코멘트 입력 다이얼로그 호출
                    showCommentDialog(holder, touchX, touchY)
                    return true
                }
                return false
            }
        })

        // container에 터치 리스너 등록 (ViewPager2의 스와이프와 충돌 방지를 위해 GestureDetector로 위임)
        holder.container.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    override fun getItemCount(): Int = images.size

    /**
     * 코멘트 입력 다이얼로그를 띄워 사용자가 제목과 내용을 입력할 수 있도록 함
     */
    private fun showCommentDialog(holder: ViewHolder, x: Float, y: Float) {
        val dialog = CommentDialogFragment()
        dialog.listener = object : CommentDialogFragment.OnCommentSavedListener {
            override fun onCommentSaved(title: String, content: String) {
                val comment = Comment(x, y, title, content)
                // API 호출로 서버에 저장
                val request = DailyCommentSaveRequest(
                    id = 0, // 서버에서 id를 할당한다면 0 또는 빈 값으로 전송
                    title = title,
                    content = content,
                    x = x,
                    y = y
                )
                RetrofitClient.service.postDailyCommentSave(dailyId = 1, request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Log.d("DrawingPagerAdapter", "코멘트 저장 성공")
                                comments.add(comment)
                                notifyDataSetChanged() // 전체 페이지 업데이트
                            } else {
                                Log.d("DrawingPagerAdapter", "코멘트 저장 실패: ${response.code()} ${response.message()}")
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.d("DrawingPagerAdapter", "저장 오류: ${t.message}")
                        }
                    })
            }
        }
        val activity = holder.itemView.context as? FragmentActivity
        dialog.show(activity?.supportFragmentManager!!, "CommentDialog")
    }

    /**
     * 주어진 코멘트 데이터를 기반으로 오버레이 뷰(TextView 등)를 생성하여 container에 추가합니다.
     */
    private fun addCommentOverlay(container: FrameLayout, comment: Comment) {
        val commentView = LayoutInflater.from(container.context)
            .inflate(R.layout.item_comment, container, false) as ImageView
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = comment.x.toInt()
        lp.topMargin = comment.y.toInt()
        container.addView(commentView, lp)
    }

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}