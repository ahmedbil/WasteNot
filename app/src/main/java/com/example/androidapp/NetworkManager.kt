package com.example.androidapp

import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import org.cs446_35.wastenot.InventoryServiceGrpc
import org.cs446_35.wastenot.InventoryServiceOuterClass.GetInventoryRequest
import org.cs446_35.wastenot.InventoryServiceOuterClass.Item
import org.cs446_35.wastenot.RecipeModel
import org.cs446_35.wastenot.RecipeServiceGrpc
import org.cs446_35.wastenot.RecipeModel.Recipe
import org.cs446_35.wastenot.RecipeServiceOuterClass.SearchRecipesByNameRequest


class NetworkManager constructor(addr: String){
    private val channel = Grpc.newChannelBuilder(addr, InsecureChannelCredentials.create()).build()
    private val inventorySvc = InventoryServiceGrpc.newBlockingStub(channel);
    private val recipeSvc = RecipeServiceGrpc.newBlockingStub(channel);

    fun getRecipes(queryName: String): List<RecipeModel.Recipe> {
        val resp = recipeSvc.searchRecipesByName(
            SearchRecipesByNameRequest.newBuilder().setQuery(queryName).build()
        )
        return resp.recipesList
    }

    fun getInventory(): List<Item> {
        val resp = inventorySvc.getInventory(
            GetInventoryRequest.newBuilder().setUserId("name").build()
        )
        return resp.itemsList
    }
}
