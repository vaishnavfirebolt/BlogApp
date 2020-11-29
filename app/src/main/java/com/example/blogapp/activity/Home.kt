package com.example.blogapp.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.blogapp.fragments.home.HomeFragment
import com.example.blogapp.fragments.profile.ProfileFragment
import com.example.blogapp.fragments.settings.SettingsFragment
import com.example.blogapp.models.Post
import com.example.blogapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.popup_add_creation.*

class Home : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cUser : FirebaseUser
    private lateinit var mAuth : FirebaseAuth
    private lateinit var popUpAddPost : Dialog
    private val reqCode = 2
    private var pickedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()
        cUser = mAuth.currentUser!!

        iniPopUp()
        setupPopupImageClick()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            popUpAddPost.show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings , R.id.nav_signout
            ), drawerLayout
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        updateNavigationHeader()
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home ->{
                    supportActionBar?.title = "Home"
                    supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment , HomeFragment()).commit()
                    true
                }
                R.id.nav_profile ->{
                    supportActionBar?.title = "Profile"
                    supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment , ProfileFragment()).commit()
                    true
                }
                R.id.nav_settings ->{
                    supportActionBar?.title = "Settings"
                    supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment , SettingsFragment()).commit()
                    true
                }
                else -> {
                    FirebaseAuth.getInstance().signOut()
                    val loginActivity = Intent(this , LoginActivity::class.java)
                    startActivity(loginActivity)
                    finish()
                    false
                }
            }
        }

    }

    private fun setupPopupImageClick() {
        popUpAddPost.popup_image?.setOnClickListener {
            if(Build.VERSION.SDK_INT >= 22){
                checkAndRequestPermission()
            } else{
                openGallery()
            }
        }
    }

    private fun checkAndRequestPermission() {
        if(ContextCompat.checkSelfPermission(this ,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )){
                Toast.makeText(this,"Please Accept for Required Permission", Toast.LENGTH_SHORT).show()
            }
            else{
                ActivityCompat.requestPermissions(this , Array(1){ Manifest.permission.READ_EXTERNAL_STORAGE },reqCode)
            }
        }
        else openGallery()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent , reqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == reqCode && data !=  null){
            pickedImageUri = data.data!!
            popUpAddPost.popup_image?.setImageURI(pickedImageUri)
            popUpAddPost.choose_image?.visibility = View.INVISIBLE
        }
    }


    private fun iniPopUp() {
        popUpAddPost = Dialog(this)
        popUpAddPost.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popUpAddPost.setContentView(R.layout.popup_add_creation)

        popUpAddPost.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popUpAddPost.window?.setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT)
        popUpAddPost.window?.attributes?.gravity = Gravity.TOP

        Glide.with(this).load(cUser.photoUrl).circleCrop().into(popUpAddPost.popup_user_image)

        popUpAddPost.popup_add.setOnClickListener{
            popUpAddPost.popup_add.visibility = View.INVISIBLE
            popUpAddPost.popup_progressBar.visibility = View.VISIBLE

            if(popUpAddPost.popup_title.text.toString().isNotEmpty() && popUpAddPost.popup_description.text.toString()
                    .isNotEmpty() && pickedImageUri != null){

                val storageReference : StorageReference = FirebaseStorage.getInstance().reference.child("blog_images")
                val imageFilePath = pickedImageUri!!.lastPathSegment?.let { storageReference.child(it) }
                imageFilePath?.putFile(pickedImageUri!!)?.addOnSuccessListener {
                    imageFilePath.downloadUrl.addOnSuccessListener {uri ->
                        val imageDownloadLink : String = uri.toString()
                        val post = Post(
                            popUpAddPost.popup_title!!.text.toString(),
                            popUpAddPost.popup_description!!.text.toString(),
                            imageDownloadLink,
                            cUser.photoUrl!!.toString()
                        )

                        addPost(post)
                    }.addOnFailureListener{e->
                        e.message?.let { it1 -> showMsg(it1) }
                        popUpAddPost.popup_add.visibility = View.VISIBLE
                        popUpAddPost.popup_progressBar.visibility = View.INVISIBLE
                    }
                }
            }
            else {
                showMsg("Verify all Fields and Choose an Image for the post")
                popUpAddPost.popup_add.visibility = View.VISIBLE
                popUpAddPost.popup_progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun addPost(post: Post) {
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef : DatabaseReference = database.getReference("Posts").push()

        val key = myRef.key
        if (key != null) {
            post.setPostKey(key)
        }
        myRef.setValue(post).addOnSuccessListener {
            showMsg("Post Added Successfully")
            popUpAddPost.popup_add!!.visibility = View.VISIBLE
            popUpAddPost.popup_progressBar!!.visibility = View.INVISIBLE
            popUpAddPost.popup_title.text.clear()
            popUpAddPost.popup_description.text.clear()

            popUpAddPost.dismiss()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun updateNavigationHeader(){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)

        headerView.nav_user_mail.text = cUser.email
        headerView.nav_username.text = cUser.displayName
        Glide.with(this).load(cUser.photoUrl).circleCrop().into(headerView.nav_user_photo)
    }
    private fun showMsg(msg: String) {
        Toast.makeText(applicationContext , msg , Toast.LENGTH_LONG).show()
    }
}

