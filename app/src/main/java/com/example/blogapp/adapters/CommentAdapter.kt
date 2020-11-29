package com.example.blogapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.models.Comment
import com.example.blogapp.R
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private var mData : MutableList<Comment>, private var mContext : Context) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val row :View = LayoutInflater.from(mContext).inflate(R.layout.row_comment,parent,false)
        return CommentViewHolder(row)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.cTvName.text = mData[position].uName
        holder.cTvContent.text = mData[position].content
        Glide.with(mContext).load(mData[position].uImg).circleCrop().into(holder.cIvUserImg)
        holder.cTvTime.text = getDateTime(mData[position].getTimeStamp().toString())
    }
    class CommentViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cTvName : TextView = itemView.findViewById(R.id.comment_username)
        var cTvContent : TextView = itemView.findViewById(R.id.comment_content)
        var cIvUserImg : ImageView = itemView.findViewById(R.id.comment_userImg)
        var cTvTime : TextView = itemView.findViewById(R.id.comment_time)
    }
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getDateTime(s : String): String? {
        return try {
            val sdf = SimpleDateFormat("hh:mm")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        }catch (e:Exception){
            e.toString()
        }
    }
}