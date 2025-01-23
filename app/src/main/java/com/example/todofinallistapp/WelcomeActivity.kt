package com.example.todofinallistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        val nameInput=findViewById<EditText>(R.id.nameInput)
        val submitButton=findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("userName", name).apply()

                val intent = Intent(this, MainScreenActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                nameInput.error = "Please enter your name"
            }
        }
    }
}