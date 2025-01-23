package com.example.todofinallistapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle logout button click
        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            firebaseAuth.signOut() // Log out from Firebase
            val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0)
            sharedPreferences.edit().clear().apply() // Clear stored username

            // Redirect to SigninActivity
            val intent = Intent(requireContext(), SigninActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}