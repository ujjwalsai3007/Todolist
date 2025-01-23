package com.example.todofinallistapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val userName = sharedPreferences.getString("userName", null)

                if (userName.isNullOrEmpty()) {
                    // Navigate to WelcomeActivity if the user's name is not set
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // Navigate to MainScreenActivity if the user's name is already set
                    val intent = Intent(this, MainScreenActivity::class.java)
                    startActivity(intent)
                }
            } else {
                // User is not signed in, redirect to SignupActivity
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}