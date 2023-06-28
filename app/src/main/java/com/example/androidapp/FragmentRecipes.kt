package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentRecipesBinding

// To get images from url to setup in image view.
import com.squareup.picasso.Picasso



/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentRecipes : Fragment() {

    private var _binding: FragmentRecipesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecipesBinding.inflate(inflater, container, false)

        // importing the recipe view model which contains recipe search business logic
        var recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        // For now we delete the recipes in our recipeViewModel before we create new ones
        // This workflow needs to be changed later on and be more stable.
        recipeViewModel.deleteRecipes();
        createRecipes(recipeViewModel);

        // Observe any changes in displayed recipes to update the Recipe View
        recipeViewModel.getRecipes().observe(viewLifecycleOwner) { recipes ->
            binding.recipeScrollHost.removeAllViews();

            recipes.forEach{recipe ->
                // Setting up specific recipe overview card for each recipe
                val recipeOverviewCard = inflater.inflate(R.layout.recipe_overview_card, null);
                val recipeName = recipeOverviewCard.findViewById<TextView>(R.id.recipe_name);
                val recipeIngredients = recipeOverviewCard.findViewById<TextView>(R.id.recipe_ingredients);
                val recipeEstimatedTime = recipeOverviewCard.findViewById<TextView>(R.id.recipe_estimated_time);
                val recipeImage = recipeOverviewCard.findViewById<ImageView>(R.id.recipe_image);

                recipeName.text = recipe.getName();
                recipeIngredients.text = getIngredientsOverview(recipe.getIngredients());
                recipeEstimatedTime.text = recipe.getEstimatedTime();
                Picasso.get().load(recipe.getImageURL()).into(recipeImage)

                // add the recipe overview card to the layout.
                binding.recipeScrollHost.addView(recipeOverviewCard)
            }
        }

        return binding.root
    }

    // Request to add recipes to the recipeViewModel.
    // The information is hardcoded which will be later provided by the search engine.
    // As such the worflow must be moved to the server/interface side. And must not be kept in client side.
    fun createRecipes(recipeViewModel: RecipeViewModel) {
        recipeViewModel.addRecipe("Lamb Biryani", listOf("plain yogurt", "skinless chicken pieces",  "basmati rice", "vegetable oil" ), "2hr 15 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2022/07/27/0/YAHI_Dum-Aloo-Biryani_s4x3.jpg.rend.hgtvcom.826.620.suffix/1658954351318.jpeg");
        recipeViewModel.addRecipe("PHỞ BÒ", listOf("Beef brisket", "lb beef shank", "cooked rice noodles" ), "8hr 25 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/11/30/0/FNK_Instant-Pot-Beef-Pho-H_s4x3.jpg.rend.hgtvcom.826.620.suffix/1548176890147.jpeg");
        recipeViewModel.addRecipe("Tonkotsu ramen", listOf("chicken carcass", "pork ribs",  "dried shiitake mushrooms"), "20hr 45 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/4/3/0/LS-Library_Kimchi-and-Bacon-Ramen_s4x3.jpg.rend.hgtvcom.826.620.suffix/1522778330680.jpeg");
    }

    // We want to only display a number of ingredients in the recipe overview card. As such
    // the function only selects a portion of ingredients to display.
    fun getIngredientsOverview(ingredients: List<String>) : String {
        val ingredientsDisplayThreshold = 3;
        var ingredientsDisplay = ""
        val numOfIngredients = ingredients.size;

        var ingredientDisplaySize = 0;

        ingredientDisplaySize = if (numOfIngredients >= ingredientsDisplayThreshold) {
            ingredientsDisplayThreshold
        } else {
            numOfIngredients
        }

        val ingredientDisplayList = ingredients.take(ingredientDisplaySize)

        var isFirstIngredient = true;

        for (ingredient in ingredientDisplayList) {
            if (isFirstIngredient) {
                ingredientsDisplay = ingredient;
                isFirstIngredient = !isFirstIngredient;
            } else {
                ingredientsDisplay += ", $ingredient"
            }
        }

        return ingredientsDisplay
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        // adding listener to search bar to catch events such as query submit and change.
        binding.searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    recipeViewModel.queryRecipes(newText);
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                recipeViewModel.queryRecipes(query);
                return false
            }

        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}