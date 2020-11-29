package com.example.blogapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.activity.PostDetailActivity
import com.example.blogapp.models.Post
import com.example.blogapp.R

class PostAdapter(private var mData : MutableList<Post>, private var mContext : Context) :
    RecyclerView.Adapter<PostAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val row :View = LayoutInflater.from(mContext).inflate(R.layout.row_post_item,parent,false)
        return MyViewHolder(row)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvTitle.text = mData[position].title
        Glide.with(mContext).load(mData[position].picture).into(holder.imgPost)
        Glide.with(mContext).load(mData[position].userPhoto).circleCrop().into(holder.imgPostProfile)
        holder.itemView.setOnClickListener{
            val postDetailActivity = Intent(mContext , PostDetailActivity::class.java)
            val pos : Int = holder.adapterPosition
            postDetailActivity.putExtra("title" , mData[pos].title)
            postDetailActivity.putExtra("description" , mData[pos].description)
            postDetailActivity.putExtra("postImg" , mData[pos].picture)
            postDetailActivity.putExtra("postKey" , mData[pos].getPostKey())
            postDetailActivity.putExtra("userPhoto" , mData[pos].userPhoto)
            postDetailActivity.putExtra("postDate" , mData[pos].getTimeStamp().toString())
            mContext.startActivity(postDetailActivity)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle : TextView = itemView.findViewById(R.id.row_post_title)
        var imgPost : ImageView = itemView.findViewById(R.id.row_post_image)
        var imgPostProfile : ImageView = itemView.findViewById(R.id.row_user_image)
    }
}