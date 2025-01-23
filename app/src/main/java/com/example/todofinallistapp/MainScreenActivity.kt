package com.example.todofinallistapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainscreen)
        // Set initial fragment
        replaceFragment(CategoriesFragment())

        // Handle BottomNavigationView item selection
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottmNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_categories -> replaceFragment(CategoriesFragment())
                R.id.menu_tasks -> replaceFragment(TasksFragment())
                R.id.menu_settings -> replaceFragment(SettingsFragment())
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}