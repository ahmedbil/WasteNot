package com.example.androidapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val metadata: Metadata
) {
    @Serializable
    data class IngredientAmount(
        val type: Int,
        val typeName: String,
        val value: Double
    )

    @Serializable
    data class Ingredient(
        val name: String,
        val amount: IngredientAmount,
        val notes: String,
        val optional: Boolean
    )

    @Serializable
    data class Metadata (
        val tags: List<String>,
        val minutes_to_prep: Int,
        val minutes_to_cook: Int,
        val minutes_total: Int,
        val difficulty: Int,
        val servings: ServingRange,
        val estimated_calories: Int,
        val image_url: String,
        val image_alt: String,
        val source_url: String,
        val dietary: DietartyMetadata
    )

    @Serializable
    data class ServingRange (
        val min: Int,
        val max: Int,
        val alternative: String
    )

    @Serializable
    data class DietartyMetadata(
        val is_vegetarian: Boolean,
        val is_vegan: Boolean,
        val is_gluten_free: Boolean,
        val is_dairy_free: Boolean,
        val is_nut_free: Boolean,
        val is_shellfish_free: Boolean,
        val is_egg_free: Boolean,
        val is_soy_free: Boolean,
        val is_fish_free: Boolean,
        val is_pork_free: Boolean,
        val is_red_meat_free: Boolean,
        val is_alcohol_free: Boolean,
        val is_kosher: Boolean,
        val is_halal: Boolean
    )
}
