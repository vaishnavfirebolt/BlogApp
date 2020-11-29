package com.example.blogapp.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.blogapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val reqCode =   1
    private lateinit var mAuth : FirebaseAuth
    private lateinit var pickedImageUri : Uri
    private var valid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        regProgressBar.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()

        userPhoto.setOnClickListener {
            if(Build.VERSION.SDK_INT >= 22){
                checkAndRequestPermission()
            } else{
                openGallery()
            }
        }
        regButton.setOnClickListener {

            if(regName?.text.toString() == ""){
                regName.error = "Name is required"
                regName.requestFocus()
                return@setOnClickListener
            }
            if( regMail?.text.toString() == ""){
                regMail.error = "Email is required"
                regMail.requestFocus()
                return@setOnClickListener
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(regMail?.text.toString()).matches()){
                regMail.error = "Please enter a valid email"
                regMail.requestFocus()
                return@setOnClickListener
            }
            if( regPassword?.text.toString() == "" || regPassword?.text.toString().length < 6){
                regPassword.error = "Password should have 6 or more characters"
                regPassword.requestFocus()
                return@setOnClickListener
            }
            if(regPassword2?.text.toString() != regPassword?.text.toString()){
                regPassword2?.error = "Password does not match"
                regPassword2?.requestFocus()
                return@setOnClickListener
            }
            if(!valid){
                showMsg("Please choose an image")
                return@setOnClickListener
            }


            regButton.visibility = View.INVISIBLE
            regProgressBar.visibility = View.VISIBLE
            registerUser(regName.text.toString() , regMail.text.toString() , regPassword.text.toString())
        }

    }

    private fun registerUser(name: String, mail: String, password: String) {
        mAuth.createUserWithEmailAndPassword(mail , password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    showMsg("Account successfully created\n"+"Please wait while we upload your photo")
                    updateUserInfo(name , mAuth.currentUser)
                }
                else{
                    showMsg("Account creation Failed" + it.exception?.message)
                    regButton.visibility = View.VISIBLE
                    regProgressBar.visibility = View.GONE
                }
              }
    }

    private fun updateUserInfo(name: String, currentUser: FirebaseUser?) {
        val mStorage = FirebaseStorage.getInstance().reference.child("users_photos")
        val imageFilePath = pickedImageUri.lastPathSegment?.let { mStorage.child(it) }
        imageFilePath?.putFile(pickedImageUri)?.addOnSuccessListener {
            imageFilePath.downloadUrl.addOnSuccessListener {it1 ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(it1)
                    .build()
                currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener{
                    showMsg("Register Complete")
                    updateUI()
                    Log.v("YES" , "done")
                }
            }
        }
    }

    private fun updateUI() {
        startActivity( Intent(this, Home::class.java))
        finish()
    }

    private fun showMsg(msg: String) {
        Toast.makeText(applicationContext , msg , Toast.LENGTH_LONG).show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent , reqCode)
    }

    private fun checkAndRequestPermission() {
        if(ContextCompat.checkSelfPermission(this , READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)){
                Toast.makeText(this,"Please Accept for Required Permission", Toast.LENGTH_SHORT).show()
            }
            else{
                ActivityCompat.requestPermissions(this , Array(1){READ_EXTERNAL_STORAGE},reqCode)
            }
        }
        else openGallery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == reqCode && data !=  null){
            pickedImageUri = data.data!!
            userPhoto.setImageURI(pickedImageUri)
            valid = true
        }
    }
}