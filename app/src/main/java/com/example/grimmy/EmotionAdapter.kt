package com.example.grimmy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.grimmy.databinding.ItemEmotionBinding

class EmotionAdapter(private var emotions: List<Emotion>) :
    RecyclerView.Adapter<EmotionAdapter.EmotionViewHolder>() {
    val emotionColors = mapOf(
        "기쁨" to R.color.happy,
        "분노" to R.color.angry,
        "슬픔" to R.color.sad,
        "집중" to R.color.lightening,
        "사랑" to R.color.love,
        "피곤" to R.color.sleepy
    )
    inner class EmotionViewHolder(val binding: ItemEmotionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionViewHolder {
        val binding = ItemEmotionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
        val emotion = emotions[position]
        holder.binding.apply {
            itemEmotionIv.setImageResource(emotion.iconRes) // 감정 아이콘 설정
            itemEmotionNameTv.text = emotion.name // 감정 이름 설정
            itemEmotionPercent.text = "${emotion.value.toInt()}%" // 퍼센트 텍스트 설정

            // 색상을 가져오고 기본값 설정
            val color = emotionColors[emotion.name]?.let {
                ContextCompat.getColor(holder.itemView.context, it)
            } ?: ContextCompat.getColor(holder.itemView.context, R.color.main_color) // 기본 색상

            itemEmotionPercent.setTextColor(color) // 텍스트 색상 설정
        }
    }

    override fun getItemCount(): Int = emotions.size

    fun updateData(newEmotions: List<Emotion>) {
        emotions = newEmotions
        notifyDataSetChanged()
    }

}