package com.example.grimmy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val dialog = builder.create()
        // 다이얼로그가 화면 하단에 붙도록 gravity와 레이아웃 파라미터를 설정합니다.
        dialog.window?.apply {
            setGravity(Gravity.BOTTOM)
            // 필요에 따라 다이얼로그의 너비를 MATCH_PARENT로 설정하여 화면 전체 너비를 사용할 수 있음
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(R.drawable.bg_rectangle)
        }
        return dialog
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