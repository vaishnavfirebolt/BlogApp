package com.example.blogapp.models

import com.google.firebase.database.ServerValue

class Comment(var content: String, var uImg: String, var uName: String) {
    private var timeStamp: Any = ServerValue.TIMESTAMP

    fun getTimeStamp(): Any {
        return timeStamp
    }

}