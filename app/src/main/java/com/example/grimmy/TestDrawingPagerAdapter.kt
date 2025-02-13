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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TestDrawingPagerAdapter(
    private val images: List<Uri>
) : RecyclerView.Adapter<TestDrawingPagerAdapter.ViewHolder>() {

    // 각 페이지별로 코멘트를 저장할 자료구조 (좌표, 제목, 내용)
    data class Comment(val x: Float, val y: Float, val title: String, val content: String)

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

        // 기존에 추가한 코멘트 뷰들을 모두 제거한 후 다시 추가 (뷰 재활용 대응)
        holder.container.removeViews(1, holder.container.childCount - 1)

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
                // 입력받은 코멘트를 저장
                val comment = Comment(x, y, title, content)
                holder.comments.add(comment)
                // 오버레이로 추가
                addCommentOverlay(holder.container, comment)
            }
        }
        val activity = holder.itemView.context as? androidx.fragment.app.FragmentActivity
        dialog.show(activity?.supportFragmentManager!!, "CommentDialog")
    }

    /**
     * 주어진 코멘트 데이터를 기반으로 오버레이 뷰(TextView 등)를 생성하여 container에 추가합니다.
     */
    private fun addCommentOverlay(container: FrameLayout, comment: Comment) {
        // item_comment.xml 레이아웃을 인플레이트하여 코멘트 뷰 생성
        val commentView = LayoutInflater.from(container.context)
            .inflate(R.layout.item_comment, container, false)

        // container 내부에 절대 위치 지정: 터치한 좌표에 맞춰 오버레이합니다.
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        // 터치 좌표를 기준으로 오프셋을 조정(예: 코멘트 뷰의 크기를 고려)
        layoutParams.leftMargin = comment.x.toInt()
        layoutParams.topMargin = comment.y.toInt()
        container.addView(commentView, layoutParams)
    }
}