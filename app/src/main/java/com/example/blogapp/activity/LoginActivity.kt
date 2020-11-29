package com.example.blogapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.blogapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        loginProgressBar.visibility = View.INVISIBLE

        loginButton.setOnClickListener {
            loginProgressBar.visibility = View.VISIBLE
            loginButton.visibility = View.INVISIBLE

            val mail = loginMail.text.toString()
            val password = loginPassword.text.toString()
            if(mail == "" || password == ""){
                showMsg("Please verify credentials")
                loginProgressBar.visibility = View.INVISIBLE
                loginButton.visibility = View.VISIBLE
            }else signIn(mail , password)

        }

        loginPhoto.setOnClickListener{
            val registerActivity = Intent(applicationContext , RegisterActivity::class.java)
            startActivity(registerActivity)
            finish()
        }

    }

    private fun signIn(mail: String, password: String) {
        mAuth.signInWithEmailAndPassword(mail , password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    loginProgressBar.visibility = View.INVISIBLE
                    loginButton.visibility = View.VISIBLE
                    updateUI()
                }else {
                    it.exception?.message?.let { it1 -> showMsg(it1) }
                    loginProgressBar.visibility = View.INVISIBLE
                    loginButton.visibility = View.VISIBLE
                }
            }
    }

    private fun updateUI() {
        val homeActivity = Intent(this , Home::class.java)
        startActivity(homeActivity)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val user : FirebaseUser? = mAuth.currentUser
        if(user != null){
            updateUI()
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(applicationContext , msg , Toast.LENGTH_SHORT).show()
    }
}