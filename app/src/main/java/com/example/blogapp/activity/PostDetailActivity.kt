package com.example.blogapp.activity

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.adapters.CommentAdapter
import com.example.blogapp.models.Comment
import com.example.blogapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.text.SimpleDateFormat
import java.util.*

class PostDetailActivity : AppCompatActivity() {

    private lateinit var cUser : FirebaseUser
    private lateinit var mAuth : FirebaseAuth
    private var postKey : String? = null
    private lateinit var database : FirebaseDatabase
    private lateinit var commentRecyclerView : RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList : MutableList<Comment>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        commentRecyclerView = this.findViewById(R.id.rv_comment)
        val w : Window = window
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS , WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        supportActionBar!!.hide()

        mAuth = FirebaseAuth.getInstance()
        cUser = mAuth.currentUser!!
        database = FirebaseDatabase.getInstance()

        val postImage = intent.extras!!.getString("postImg")
        Glide.with(this).load(postImage).into(post_details_image)

        val postTitle = intent.extras!!.getString("title")
        post_details_title.text = postTitle

        val userPostImg = intent.extras!!.getString("userPhoto")
        Glide.with(this).load(userPostImg).circleCrop().into(post_detail_postuser_image)

        val postDescription = intent.extras!!.getString("description")
        post_detail_description.text = postDescription

        Glide.with(this).load(cUser.photoUrl).circleCrop().into(post_detail_currentuser_image)

        postKey = intent.extras!!.getString("postKey")
        val date = intent.extras!!.getString("postDate")?.let { getDateTime(it) }

        post_detail_date_name.text = date


        post_detail_addComment_button.setOnClickListener{

           if(post_detail_edittext_comment.toString().isNotEmpty() && post_detail_edittext_comment.toString() != ""){

               val comment = Comment(
                   post_detail_edittext_comment.text.toString(),
                   cUser.photoUrl!!.toString(),
                   cUser.displayName!!
               )
               addComment(comment)
           }else showMsg("Please type something in the comment box")
        }

        initCommentRecyclerView()

    }

    private fun initCommentRecyclerView() {

        commentRecyclerView.layoutManager = LinearLayoutManager(this)

        val commentRef : DatabaseReference = database.getReference("Comment").child(postKey!!)
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                showMsg(error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                commentList = mutableListOf()
                snapshot.children.forEach{
                    val comment : Comment? = it.getValue(Comment::class.java)
                    commentList.add(comment!!)
                }
                commentList.reverse()
                commentAdapter = CommentAdapter(commentList , applicationContext)
                commentRecyclerView.adapter = commentAdapter
            }

        })
    }

    private fun addComment(comment: Comment) {
        post_detail_addComment_button.visibility = View.INVISIBLE
        val commentReference : DatabaseReference = database.getReference("Comment").child(postKey!!).push()

        commentReference.setValue(comment).addOnSuccessListener {
            showMsg("Comment Added")
            post_detail_edittext_comment.text.clear()
            post_detail_addComment_button.visibility = View.VISIBLE
        }.addOnFailureListener{
            showMsg("FAILED TO ADD COMMENT" + it.message)
            post_detail_addComment_button.visibility = View.VISIBLE
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(applicationContext , msg , Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getDateTime(s : String): String? {
        return try {
            val sdf = SimpleDateFormat("MM-dd-yyyy")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        }catch (e:Exception){
            e.toString()
        }
    }

}