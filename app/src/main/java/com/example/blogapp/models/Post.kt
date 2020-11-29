package com.example.blogapp.models

import com.google.firebase.database.ServerValue

class Post(
    var title: String,
    var description: String,
    var picture: String,
    var userPhoto: String
) {
    private var timeStamp: Any = ServerValue.TIMESTAMP
    private lateinit var postKey : String

    fun getPostKey(): String {
        return postKey
    }
    fun setPostKey(postKey : String){
        this.postKey = postKey
    }

    fun getTimeStamp(): Any {
        return timeStamp
    }
}