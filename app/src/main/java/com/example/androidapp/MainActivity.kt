package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.androidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nwManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        nwManager = NetworkManager.initialize(applicationContext) {
            nwManager.getHeartbeat()
            if (!it.isSignedIn) {
                // Authenticate the user first.
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }

        setContentView(binding.root)

        // Load the recipes page by default.
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentRecipes()).commit()

        // Set up listeners for the bottom navigation drawer.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_recipes -> {
                    binding.topAppBar.setTitle(R.string.recipes_title)
                    // update the action bar for recipes search page
                    updateActionBar();
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentRecipes()).commit()
                    true
                }

                R.id.page_inventory -> {
                    binding.topAppBar.setTitle(R.string.inventory_title)
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
        val recipeFilterButton = binding.topAppBar.menu.findItem(R.id.action_recipe_filter)

        //enable filter button if it is recipe search page.
        recipeFilterButton.isVisible = binding.topAppBar.title == "Recipes"
    }
}