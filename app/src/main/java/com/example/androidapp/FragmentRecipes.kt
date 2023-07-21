package com.example.androidapp

// To get images from url to setup in image view.
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentRecipesBinding
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import java.io.IOException


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
        recipeViewModel.queryRecipes("");

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

                // requires recipeImage to be not null.
                Picasso.get().load(recipe.getImageURL()).into(recipeImage)

                // add the recipe overview card to the layout.
                binding.recipeScrollHost.addView(recipeOverviewCard)
            }
        }

        var image : Bitmap? = null


        //https://ocr.space/Content/Images/receipt-ocr-original.jpg
        Picasso.get().load("https://i.postimg.cc/mZd1ng21/1111-receipt.jpg").into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // Image loaded successfully, notify the callback
                //image = bitmap
                //val scanner = ReceiptScanner();
                //val image = Picasso.get().load("https://ocr.space/Content/Images/receipt-ocr-original.jpg").into(imageview)
                //Log.i("image-receipt", image.toString())
                //scanner.parseReceiptImage(image);
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                // Failed to load image, notify the callback
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Image is being prepared, do nothing
            }
        })
        return binding.root
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

        val recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        // adding listener to search bar to catch events such as query submit and change.
        binding.recipeSearchField.editText?.addTextChangedListener {
            recipeViewModel.queryRecipes(it.toString());
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}