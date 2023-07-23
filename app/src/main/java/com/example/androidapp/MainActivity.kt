package com.example.androidapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.androidapp.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navbarView: BottomNavigationView
    private lateinit var fragmentContainer: ConstraintLayout
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var nwManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nwManager = NetworkManager.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navbarView = findViewById(R.id.bottom_navigation)
        fragmentContainer = findViewById(R.id.fragment_container)
        topAppBar = findViewById(R.id.top_app_bar)

        // Load the recipes page by default.
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentRecipes()).commit()

        // Set up listeners for the bottom navigation drawer.
        navbarView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_recipes -> {
                    topAppBar.setTitle(R.string.recipes_title)
                    // update the action bar for recipes search page
                    updateActionBar();
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentRecipes()).commit()
                    true
                }

                R.id.page_inventory -> {
                    topAppBar.setTitle(R.string.inventory_title)
                    // update the action bar for inventory page
                    updateActionBar();
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentReceiptScanner()).commit()
                    true
                }

                else -> false
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_recipe_filter -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Updates the action bar based on the specific fragment page
    fun updateActionBar() {
        val recipeFilterButton = topAppBar.menu.findItem(R.id.action_recipe_filter)

        //enable filter button if it is recipe search page.
        recipeFilterButton.isVisible = topAppBar.title == "Recipes"
    }
}