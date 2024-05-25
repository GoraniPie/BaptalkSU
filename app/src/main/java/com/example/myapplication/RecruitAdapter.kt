package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecruitAdapter(private var recruitList: List<RecruitDataModel>) :
    RecyclerView.Adapter<RecruitAdapter.RecruitViewHolder>() {

    inner class RecruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recruitTitle)
        val place: TextView = itemView.findViewById(R.id.recruitBaptime)
        val createdAt: TextView = itemView.findViewById(R.id.recruitUploader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit, parent, false)
        return RecruitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecruitViewHolder, position: Int) {
        val recruit = recruitList[position]
        holder.title.text = recruit.title
        holder.place.text = recruit.place
        holder.createdAt.text = recruit.created_at?.toDate().toString()
    }

    override fun getItemCount() = recruitList.size

    fun updateList(newList: List<RecruitDataModel>) {
        recruitList = newList
        notifyDataSetChanged()
    }
}