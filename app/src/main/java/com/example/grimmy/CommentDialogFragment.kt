package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

class CommentDialogFragment : DialogFragment() {

    interface OnCommentSavedListener {
        fun onCommentSaved(title: String, content: String)
    }
    var listener: OnCommentSavedListener? = null

    // 다이얼로그의 뷰를 저장할 변수
    private var dialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        dialogView = LayoutInflater.from(context).inflate(R.layout.layout_comment_input, null)
        builder.setView(dialogView)
        // 버튼은 커스텀 레이아웃에 있으므로 AlertDialog의 기본 버튼은 사용하지 않음
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val alertDialog = dialog as? AlertDialog
        // 저장 버튼이 ImageView라면 ImageView로 찾습니다.
        val saveButton = alertDialog?.findViewById<ImageView>(R.id.comment_save_btn_iv)
        saveButton?.setOnClickListener {
            val title = alertDialog.findViewById<EditText>(R.id.comment_title_et)?.text.toString()
            val content = alertDialog.findViewById<EditText>(R.id.comment_content_et)?.text.toString()
            // 제목과 내용 모두 입력되어 있을 때만 저장
            if (title.isNotEmpty() && content.isNotEmpty()) {
                listener?.onCommentSaved(title, content)
            }
            dismiss()  // 저장 후 다이얼로그 종료
        }
    }
}