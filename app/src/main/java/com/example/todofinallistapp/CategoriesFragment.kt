package com.example.todofinallistapp

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "there")
        greetingText.text = "What's up, $userName!"


        val database = AppDatabase.getDatabase(requireContext())
        val taskDao = database.taskDao()

        // Fetch categories dynamically
        lifecycleScope.launch {
            val categories = listOf("Work", "Personal", "Shopping", "Fitness").map { category ->
                val taskCount = taskDao.getTaskCountByCategory(category)
                Category(category, taskCount)
            }

            // Set RecyclerView adapter
            recyclerView.adapter = CategoriesAdapter(categories) { clickedCategory ->
                // Navigate to TasksFragment when a category is clicked
                val tasksFragment = TasksFragment().apply {
                    arguments = Bundle().apply {
                        putString("category", clickedCategory.name)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, tasksFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}