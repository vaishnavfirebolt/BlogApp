package com.example.blogapp.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.adapters.PostAdapter
import com.example.blogapp.models.Post
import com.example.blogapp.R
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var postRecyclerView : RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var postList : MutableList<Post>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView : View = inflater.inflate(R.layout.fragment_home, container, false)
        postRecyclerView = fragmentView.findViewById(R.id.postRV)
        postRecyclerView.layoutManager = LinearLayoutManager(activity , RecyclerView.VERTICAL , false)
        postRecyclerView.setHasFixedSize(true)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Posts")
        return fragmentView
    }

    override fun onStart() {
        super.onStart()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context , error.toString() , Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                postList = mutableListOf()
                snapshot.children.forEach{
                    val post : Post? = it.getValue(Post::class.java)
                    postList.add(post!!)
                }
                postList.reverse()
                postAdapter = PostAdapter(postList, activity!!)
                postRecyclerView.adapter = postAdapter
            }

        })
    }
}