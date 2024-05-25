package com.example.myapplication

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecruitAdapter(private var recruitList: List<RecruitDataModel>) :
    RecyclerView.Adapter<RecruitAdapter.RecruitViewHolder>() {

    inner class RecruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recruitTitle)
        val baptime: TextView = itemView.findViewById(R.id.recruitBaptime)
        val place: TextView = itemView.findViewById(R.id.recruitPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit, parent, false)
        return RecruitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecruitViewHolder, position: Int) {
        val recruit = recruitList[position]
        holder.title.text = recruit.title
        holder.place.text = "식사 장소 : ${recruit.place}"

        val date = recruit.baptime?.toDate()
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
        val minute = calendar.get(Calendar.MINUTE).toString()
        holder.baptime.text = "식사 시간 : ${hour}시 ${minute}분"
    }

    override fun getItemCount() = recruitList.size

    fun updateList(newList: List<RecruitDataModel>) {
        recruitList = newList
        notifyDataSetChanged()
    }
}