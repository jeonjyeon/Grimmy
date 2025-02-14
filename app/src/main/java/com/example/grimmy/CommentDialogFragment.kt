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
        val alertDialog = dialog as? AlertDialog ?: return

        // 레이아웃에서 EditText와 저장 버튼(ImageView) 참조
        val titleEditText = alertDialog.findViewById<EditText>(R.id.comment_title_et)
        val contentEditText = alertDialog.findViewById<EditText>(R.id.comment_content_et)
        val saveButton = alertDialog.findViewById<ImageView>(R.id.comment_save_btn_iv)

        // read-only 모드를 위한 플래그와 초기 값 전달
        val isReadOnly = arguments?.getBoolean("isReadOnly") ?: false
        if (isReadOnly) {
            // 읽기 전용 모드: 번들에 담긴 초기 제목/내용을 설정하고 EditText를 비활성화
            val initialTitle = arguments?.getString("initialTitle") ?: ""
            val initialContent = arguments?.getString("initialContent") ?: ""
            titleEditText?.setText(initialTitle)
            contentEditText?.setText(initialContent)
            titleEditText?.isEnabled = false
            contentEditText?.isEnabled = false

            // 저장 버튼 클릭 시 단순히 다이얼로그를 종료 (편집 불가)
            saveButton?.setOnClickListener {
                dismiss()
            }
        } else {
            // 일반 모드: 사용자가 제목/내용을 수정할 수 있도록 하고 저장 버튼 클릭 시 listener 호출
            saveButton?.setOnClickListener {
                val newTitle = titleEditText?.text.toString()
                val newContent = contentEditText?.text.toString()
                if (newTitle.isNotEmpty() && newContent.isNotEmpty()) {
                    listener?.onCommentSaved(newTitle, newContent)
                }
                dismiss()
            }
        }
    }
}